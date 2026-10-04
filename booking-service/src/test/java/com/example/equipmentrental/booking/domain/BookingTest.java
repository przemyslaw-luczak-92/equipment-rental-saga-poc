package com.example.equipmentrental.booking.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    @Test
    void shouldCreatePendingBooking() {
        Booking booking = newBooking();

        assertEquals(BookingStatus.PENDING, booking.status());
        assertTrue(booking.rejectionReason().isEmpty());
    }

    @Test
    void shouldConfirmPendingBooking() {
        Booking booking = newBooking();

        assertTrue(booking.confirm());
        assertEquals(BookingStatus.CONFIRMED, booking.status());
    }

    @Test
    void shouldConfirmOnlyOnce() {
        Booking booking = newBooking();

        assertTrue(booking.confirm());
        assertFalse(booking.confirm());
        assertEquals(BookingStatus.CONFIRMED, booking.status());
    }

    @Test
    void shouldRejectPendingBooking() {
        Booking booking = newBooking();

        assertTrue(booking.reject(
                BookingRejectionReason.INSUFFICIENT_STOCK
        ));

        assertEquals(BookingStatus.REJECTED, booking.status());
        assertEquals(
                BookingRejectionReason.INSUFFICIENT_STOCK,
                booking.rejectionReason().orElseThrow()
        );
    }

    @Test
    void shouldNotOverwriteFirstRejection() {
        Booking booking = newBooking();

        assertTrue(booking.reject(
                BookingRejectionReason.INSUFFICIENT_STOCK
        ));
        assertFalse(booking.reject(
                BookingRejectionReason.CUSTOMER_LIMIT_EXCEEDED
        ));

        assertEquals(
                BookingRejectionReason.INSUFFICIENT_STOCK,
                booking.rejectionReason().orElseThrow()
        );
    }

    @Test
    void shouldNotRejectConfirmedBooking() {
        Booking booking = newBooking();
        booking.confirm();

        assertThrows(
                IllegalStateException.class,
                () -> booking.reject(
                        BookingRejectionReason.INSUFFICIENT_STOCK
                )
        );

        assertEquals(BookingStatus.CONFIRMED, booking.status());
    }

    @Test
    void shouldNotConfirmRejectedBooking() {
        Booking booking = newBooking();
        booking.reject(BookingRejectionReason.UNKNOWN_EQUIPMENT);

        assertThrows(
                IllegalStateException.class,
                booking::confirm
        );

        assertEquals(BookingStatus.REJECTED, booking.status());
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.pending(
                        BOOKING_ID,
                        "customer-normal",
                        "camera",
                        0
                )
        );
    }

    @Test
    void shouldRestoreConfirmedBooking() {
        Booking booking = Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.CONFIRMED,
                null
        );

        assertEquals(BookingStatus.CONFIRMED, booking.status());
        assertFalse(booking.confirm());
        assertTrue(booking.rejectionReason().isEmpty());
    }

    @Test
    void shouldRestoreRejectedBooking() {
        Booking booking = Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.REJECTED,
                BookingRejectionReason.INSUFFICIENT_STOCK
        );

        assertEquals(BookingStatus.REJECTED, booking.status());
        assertEquals(
                BookingRejectionReason.INSUFFICIENT_STOCK,
                booking.rejectionReason().orElseThrow()
        );
    }

    @Test
    void shouldRejectRestoredRejectedStateWithoutReason() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Booking.restore(
                        BOOKING_ID,
                        "customer-normal",
                        "camera",
                        2,
                        BookingStatus.REJECTED,
                        null
                )
        );
    }

    private Booking newBooking() {
        return Booking.pending(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2
        );
    }
}