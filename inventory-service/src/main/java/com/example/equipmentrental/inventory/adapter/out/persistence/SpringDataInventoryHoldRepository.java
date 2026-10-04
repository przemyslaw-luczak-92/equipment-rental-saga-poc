package com.example.equipmentrental.inventory.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataInventoryHoldRepository
        extends JpaRepository<InventoryHoldJpaEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventoryHold
            from InventoryHoldJpaEntity inventoryHold
            where inventoryHold.bookingId = :bookingId
            """)
    Optional<InventoryHoldJpaEntity> findByBookingIdForUpdate(
            @Param("bookingId") UUID bookingId
    );
}