package com.example.equipmentrental.inventory.adapter.out.persistence;

import com.example.equipmentrental.inventory.application.port.out.InventoryHoldRepository;
import com.example.equipmentrental.inventory.domain.InventoryHold;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InventoryHoldPersistenceAdapter implements InventoryHoldRepository {

    private final SpringDataInventoryHoldRepository repository;

    public InventoryHoldPersistenceAdapter(SpringDataInventoryHoldRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<InventoryHold> findByBookingIdForUpdate(UUID bookingId) {
        return repository
                .findByBookingIdForUpdate(bookingId)
                .map(this::toDomain);
    }

    @Override
    public void save(InventoryHold inventoryHold) {
        repository.save(toEntity(inventoryHold));
    }

    private InventoryHold toDomain(InventoryHoldJpaEntity entity) {
        return InventoryHold.restore(
                entity.getBookingId(),
                entity.getEquipmentId(),
                entity.getQuantity(),
                entity.getStatus(),
                entity.getRefusalReason()
        );
    }

    private InventoryHoldJpaEntity toEntity(InventoryHold inventoryHold) {
        return new InventoryHoldJpaEntity(
                inventoryHold.bookingId(),
                inventoryHold.equipmentId(),
                inventoryHold.quantity(),
                inventoryHold.status(),
                inventoryHold.refusalReason().orElse(null)
        );
    }
}