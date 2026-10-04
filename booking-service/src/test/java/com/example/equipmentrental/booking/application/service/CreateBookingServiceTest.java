package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.application.port.out.CreateBookingSagaStarter;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateBookingServiceTest {

    private FakeBookingRepository bookings;
    private FakeCreateBookingSagaStarter sagaStarter;
    private CreateBookingService service;

    @BeforeEach
    void setUp() {
        bookings = new FakeBookingRepository();
        sagaStarter = new FakeCreateBookingSagaStarter();

        service = new CreateBookingService(
                bookings,
                sagaStarter
        );
    }

    @Test
    void shouldCreatePendingBookingAndStartSaga() {
        UUID bookingId = service.create(
                "customer-normal",
                "camera",
                2
        );

        assertEquals(bookingId, bookings.saved.bookingId());
        assertEquals("customer-normal", bookings.saved.customerId());
        assertEquals("camera", bookings.saved.equipmentId());
        assertEquals(2, bookings.saved.quantity());
        assertEquals(BookingStatus.PENDING, bookings.saved.status());

        assertEquals(
                bookingId,
                sagaStarter.started.bookingId()
        );
    }

    @Test
    void shouldNotSaveOrStartSagaForInvalidQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        "customer-normal",
                        "camera",
                        0
                )
        );

        assertNull(bookings.saved);
        assertNull(sagaStarter.started);
    }

    private static final class FakeBookingRepository implements BookingRepository {

        private Booking saved;

        @Override
        public Optional<Booking> findById(UUID bookingId) {
            return Optional.empty();
        }

        @Override
        public Optional<Booking> findByIdForUpdate(UUID bookingId) {
            return Optional.empty();
        }

        @Override
        public void save(Booking booking) {
            saved = booking;
        }
    }

    private static final class FakeCreateBookingSagaStarter implements CreateBookingSagaStarter {

        private Booking started;

        @Override
        public void start(Booking booking) {
            started = booking;
        }
    }
}