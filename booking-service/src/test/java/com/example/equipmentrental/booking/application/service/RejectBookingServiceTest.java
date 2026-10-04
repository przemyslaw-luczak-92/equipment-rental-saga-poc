package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.RejectBookingResult;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import com.example.equipmentrental.booking.domain.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RejectBookingServiceTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    private FakeBookingRepository bookings;
    private RejectBookingService service;

    @BeforeEach
    void setUp() {
        bookings = new FakeBookingRepository();
        service = new RejectBookingService(bookings);
    }

    @Test
    void shouldRejectPendingBooking() {
        bookings.current = pendingBooking();

        RejectBookingResult result = service.reject(
                BOOKING_ID,
                BookingRejectionReason.INSUFFICIENT_STOCK
        );

        assertEquals(RejectBookingResult.REJECTED, result);
        assertEquals(BookingStatus.REJECTED, bookings.saved.status());
        assertEquals(
                BookingRejectionReason.INSUFFICIENT_STOCK,
                bookings.saved.rejectionReason().orElseThrow()
        );
    }

    @Test
    void shouldReturnPreviousResultWhenRepeated() {
        bookings.current = rejectedBooking(
                BookingRejectionReason.UNKNOWN_EQUIPMENT
        );

        RejectBookingResult result = service.reject(
                BOOKING_ID,
                BookingRejectionReason.UNKNOWN_EQUIPMENT
        );

        assertEquals(
                RejectBookingResult.ALREADY_REJECTED,
                result
        );
        assertNull(bookings.saved);
    }

    @Test
    void shouldNotOverwriteFirstRejectionReason() {
        bookings.current = rejectedBooking(
                BookingRejectionReason.INSUFFICIENT_STOCK
        );

        RejectBookingResult result = service.reject(
                BOOKING_ID,
                BookingRejectionReason.CUSTOMER_LIMIT_EXCEEDED
        );

        assertEquals(
                RejectBookingResult.ALREADY_REJECTED,
                result
        );
        assertEquals(
                BookingRejectionReason.INSUFFICIENT_STOCK,
                bookings.current.rejectionReason().orElseThrow()
        );
        assertNull(bookings.saved);
    }

    @Test
    void shouldNotRejectConfirmedBooking() {
        bookings.current = Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.CONFIRMED,
                null
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.reject(
                        BOOKING_ID,
                        BookingRejectionReason.INSUFFICIENT_STOCK
                )
        );

        assertNull(bookings.saved);
    }

    private Booking pendingBooking() {
        return Booking.pending(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2
        );
    }

    private Booking rejectedBooking(BookingRejectionReason reason) {
        return Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.REJECTED,
                reason
        );
    }

    private static final class FakeBookingRepository implements BookingRepository {

        private Booking current;
        private Booking saved;

        @Override
        public Optional<Booking> findById(UUID bookingId) {
            return Optional.ofNullable(current);
        }

        @Override
        public Optional<Booking> findByIdForUpdate(UUID bookingId) {
            return Optional.ofNullable(current);
        }

        @Override
        public void save(Booking booking) {
            saved = booking;
            current = booking;
        }
    }
}