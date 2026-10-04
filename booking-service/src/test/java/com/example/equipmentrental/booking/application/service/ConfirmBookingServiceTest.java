package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.ConfirmBookingResult;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.application.port.out.CustomerQuotaRepository;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import com.example.equipmentrental.booking.domain.BookingStatus;
import com.example.equipmentrental.booking.domain.CustomerQuota;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfirmBookingServiceTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    private FakeBookingRepository bookings;
    private FakeCustomerQuotaRepository quotas;
    private ConfirmBookingService service;

    @BeforeEach
    void setUp() {
        bookings = new FakeBookingRepository();
        quotas = new FakeCustomerQuotaRepository();
        service = new ConfirmBookingService(bookings, quotas);
    }

    @Test
    void shouldConfirmBookingAndConsumeQuota() {
        bookings.current = pendingBooking();
        quotas.current = new CustomerQuota(
                "customer-normal",
                2,
                0
        );

        ConfirmBookingResult result = service.confirm(BOOKING_ID);

        assertEquals(ConfirmBookingResult.CONFIRMED, result);
        assertEquals(BookingStatus.CONFIRMED, bookings.saved.status());
        assertEquals(1, quotas.saved.confirmedCount());
    }

    @Test
    void shouldRefuseWhenCustomerLimitIsExceeded() {
        bookings.current = pendingBooking();
        quotas.current = new CustomerQuota(
                "customer-compensation",
                0,
                0
        );

        ConfirmBookingResult result = service.confirm(BOOKING_ID);

        assertEquals(
                ConfirmBookingResult.CUSTOMER_LIMIT_EXCEEDED,
                result
        );
        assertEquals(BookingStatus.PENDING, bookings.current.status());
        assertNull(bookings.saved);
        assertNull(quotas.saved);
    }

    @Test
    void shouldNotConsumeQuotaWhenConfirmationIsRepeated() {
        bookings.current = Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.CONFIRMED,
                null
        );

        ConfirmBookingResult result = service.confirm(BOOKING_ID);

        assertEquals(ConfirmBookingResult.CONFIRMED, result);
        assertNull(bookings.saved);
        assertNull(quotas.saved);
    }

    @Test
    void shouldNotConfirmRejectedBooking() {
        bookings.current = Booking.restore(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2,
                BookingStatus.REJECTED,
                BookingRejectionReason.INSUFFICIENT_STOCK
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.confirm(BOOKING_ID)
        );

        assertNull(bookings.saved);
        assertNull(quotas.saved);
    }

    private Booking pendingBooking() {
        return Booking.pending(
                BOOKING_ID,
                "customer-normal",
                "camera",
                2
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

    private static final class FakeCustomerQuotaRepository implements CustomerQuotaRepository {

        private CustomerQuota current;
        private CustomerQuota saved;

        @Override
        public Optional<CustomerQuota> findByCustomerId(String customerId) {
            return Optional.ofNullable(current);
        }

        @Override
        public Optional<CustomerQuota> findByCustomerIdForUpdate(String customerId) {
            return Optional.ofNullable(current);
        }

        @Override
        public void save(CustomerQuota customerQuota) {
            saved = customerQuota;
            current = customerQuota;
        }
    }
}