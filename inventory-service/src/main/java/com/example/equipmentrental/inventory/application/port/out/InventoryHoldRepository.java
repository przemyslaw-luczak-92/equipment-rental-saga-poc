package com.example.equipmentrental.inventory.application.port.out;

import com.example.equipmentrental.inventory.domain.InventoryHold;

import java.util.Optional;
import java.util.UUID;

public interface InventoryHoldRepository {

    Optional<InventoryHold> findByBookingIdForUpdate(UUID bookingId);

    void save(InventoryHold inventoryHold);
}