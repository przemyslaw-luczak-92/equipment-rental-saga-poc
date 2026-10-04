package com.example.equipmentrental.inventory.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class InventoryHold {

    private final UUID bookingId;
    private final String equipmentId;
    private final int quantity;
    private InventoryHoldStatus status;
    private final InventoryHoldRefusalReason refusalReason;

    private InventoryHold(UUID bookingId, String equipmentId, int quantity, InventoryHoldStatus status,
            InventoryHoldRefusalReason refusalReason) {

        this.bookingId = Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("equipmentId must not be blank");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }

        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );

        if (status == InventoryHoldStatus.REFUSED
                && refusalReason == null) {
            throw new IllegalArgumentException(
                    "Refused hold must have a refusal reason"
            );
        }

        if (status != InventoryHoldStatus.REFUSED
                && refusalReason != null) {
            throw new IllegalArgumentException(
                    "Only refused hold can have a refusal reason"
            );
        }

        this.refusalReason = refusalReason;
    }

    public static InventoryHold held(UUID bookingId, String equipmentId, int quantity) {
        return new InventoryHold(
                bookingId,
                equipmentId,
                quantity,
                InventoryHoldStatus.HELD,
                null
        );
    }

    public static InventoryHold refused(UUID bookingId, String equipmentId, int quantity, InventoryHoldRefusalReason refusalReason) {
        return new InventoryHold(
                bookingId,
                equipmentId,
                quantity,
                InventoryHoldStatus.REFUSED,
                Objects.requireNonNull(
                        refusalReason,
                        "refusalReason must not be null"
                )
        );
    }

    public static InventoryHold restore(UUID bookingId, String equipmentId, int quantity, InventoryHoldStatus status,
            InventoryHoldRefusalReason refusalReason) {

        return new InventoryHold(
                bookingId,
                equipmentId,
                quantity,
                status,
                refusalReason
        );
    }

    public boolean matches(String equipmentId, int quantity) {
        return this.equipmentId.equals(equipmentId)
                && this.quantity == quantity;
    }

    public boolean release() {
        if (status != InventoryHoldStatus.HELD) {
            return false;
        }

        status = InventoryHoldStatus.RELEASED;
        return true;
    }

    public UUID bookingId() {
        return bookingId;
    }

    public String equipmentId() {
        return equipmentId;
    }

    public int quantity() {
        return quantity;
    }

    public InventoryHoldStatus status() {
        return status;
    }

    public Optional<InventoryHoldRefusalReason> refusalReason() {
        return Optional.ofNullable(refusalReason);
    }
}