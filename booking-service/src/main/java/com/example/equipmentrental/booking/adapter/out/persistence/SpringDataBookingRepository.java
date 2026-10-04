package com.example.equipmentrental.booking.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataBookingRepository
        extends JpaRepository<BookingJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select booking
            from BookingJpaEntity booking
            where booking.bookingId = :bookingId
            """)
    Optional<BookingJpaEntity> findByIdForUpdate(
            @Param("bookingId") UUID bookingId
    );
}