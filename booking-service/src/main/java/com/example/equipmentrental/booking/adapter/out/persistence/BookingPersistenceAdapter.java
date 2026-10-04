package com.example.equipmentrental.booking.adapter.out.persistence;

import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.domain.Booking;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class BookingPersistenceAdapter implements BookingRepository {

    private final SpringDataBookingRepository repository;

    public BookingPersistenceAdapter(SpringDataBookingRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Booking> findById(UUID bookingId) {
        return repository
                .findById(bookingId)
                .map(this::toDomain);
    }

    @Override
    public Optional<Booking> findByIdForUpdate(UUID bookingId) {
        return repository
                .findByIdForUpdate(bookingId)
                .map(this::toDomain);
    }

    @Override
    public void save(Booking booking) {
        repository.save(toEntity(booking));
    }

    private Booking toDomain(BookingJpaEntity entity) {
        return Booking.restore(
                entity.getBookingId(),
                entity.getCustomerId(),
                entity.getEquipmentId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getRejectionReason()
        );
    }

    private BookingJpaEntity toEntity(Booking booking) {
        return new BookingJpaEntity(
                booking.bookingId(),
                booking.customerId(),
                booking.equipmentId(),
                booking.quantity(),
                booking.status(),
                booking.rejectionReason().orElse(null)
        );
    }
}