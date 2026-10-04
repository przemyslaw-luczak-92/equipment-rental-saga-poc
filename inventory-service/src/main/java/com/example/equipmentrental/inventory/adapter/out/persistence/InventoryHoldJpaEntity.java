package com.example.equipmentrental.inventory.adapter.out.persistence;

import com.example.equipmentrental.inventory.domain.InventoryHoldRefusalReason;
import com.example.equipmentrental.inventory.domain.InventoryHoldStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "inventory_hold")
public class InventoryHoldJpaEntity {

    @Id
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    @Column(name = "equipment_id", nullable = false, length = 100)
    private String equipmentId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InventoryHoldStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", length = 50)
    private InventoryHoldRefusalReason refusalReason;

    protected InventoryHoldJpaEntity() {
        // Konstruktor wymagany przez JPA.
    }

    public InventoryHoldJpaEntity(UUID bookingId, String equipmentId, int quantity, InventoryHoldStatus status,
            InventoryHoldRefusalReason refusalReason) {
        this.bookingId = bookingId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.status = status;
        this.refusalReason = refusalReason;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public InventoryHoldStatus getStatus() {
        return status;
    }

    public InventoryHoldRefusalReason getRefusalReason() {
        return refusalReason;
    }
}