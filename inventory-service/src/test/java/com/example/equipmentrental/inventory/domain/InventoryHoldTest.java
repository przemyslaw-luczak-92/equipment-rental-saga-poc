package com.example.equipmentrental.inventory.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryHoldTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    @Test
    void shouldCreateHeldHold() {
        InventoryHold hold = InventoryHold.held(BOOKING_ID, "camera", 2);

        assertEquals(InventoryHoldStatus.HELD, hold.status());
        assertTrue(hold.refusalReason().isEmpty());
    }

    @Test
    void shouldRememberRefusalReason() {
        InventoryHold hold = InventoryHold.refused(
                BOOKING_ID,
                "camera",
                2,
                InventoryHoldRefusalReason.INSUFFICIENT_STOCK
        );

        assertEquals(InventoryHoldStatus.REFUSED, hold.status());
        assertEquals(
                InventoryHoldRefusalReason.INSUFFICIENT_STOCK,
                hold.refusalReason().orElseThrow()
        );
    }

    @Test
    void shouldRecognizeMatchingAndConflictingRequests() {
        InventoryHold hold = InventoryHold.held(BOOKING_ID, "camera", 2);

        assertTrue(hold.matches("camera", 2));
        assertFalse(hold.matches("projector", 2));
        assertFalse(hold.matches("camera", 3));
    }

    @Test
    void shouldReleaseHeldHoldOnlyOnce() {
        InventoryHold hold = InventoryHold.held(BOOKING_ID, "camera", 2);

        assertTrue(hold.release());
        assertEquals(InventoryHoldStatus.RELEASED, hold.status());

        assertFalse(hold.release());
        assertEquals(InventoryHoldStatus.RELEASED, hold.status());
    }

    @Test
    void shouldNotReleaseRefusedHold() {
        InventoryHold hold = InventoryHold.refused(
                BOOKING_ID,
                "camera",
                2,
                InventoryHoldRefusalReason.UNKNOWN_EQUIPMENT
        );

        assertFalse(hold.release());
        assertEquals(InventoryHoldStatus.REFUSED, hold.status());
    }

    @Test
    void shouldRestoreReleasedHold() {
        InventoryHold hold = InventoryHold.restore(
                BOOKING_ID,
                "camera",
                2,
                InventoryHoldStatus.RELEASED,
                null
        );

        assertEquals(InventoryHoldStatus.RELEASED, hold.status());
        assertFalse(hold.release());
        assertTrue(hold.refusalReason().isEmpty());
    }

    @Test
    void shouldRejectRefusedStateWithoutReason() {
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryHold.restore(
                        BOOKING_ID,
                        "camera",
                        2,
                        InventoryHoldStatus.REFUSED,
                        null
                )
        );
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> InventoryHold.held(BOOKING_ID, "camera", 0)
        );
    }
}