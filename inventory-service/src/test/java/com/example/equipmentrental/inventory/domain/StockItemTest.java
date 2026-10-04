package com.example.equipmentrental.inventory.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockItemTest {

    @Test
    void shouldCalculateAvailableQuantity() {
        StockItem stockItem = new StockItem("camera", 5, 2);

        assertEquals(3, stockItem.available());
    }

    @Test
    void shouldHoldAvailableItems() {
        StockItem stockItem = new StockItem("camera", 5, 1);

        boolean held = stockItem.tryHold(3);

        assertTrue(held);
        assertEquals(4, stockItem.held());
        assertEquals(1, stockItem.available());
    }

    @Test
    void shouldNotHoldMoreItemsThanAvailable() {
        StockItem stockItem = new StockItem("camera", 5, 2);

        boolean held = stockItem.tryHold(4);

        assertFalse(held);
        assertEquals(2, stockItem.held());
        assertEquals(3, stockItem.available());
    }

    @Test
    void shouldReleaseHeldItems() {
        StockItem stockItem = new StockItem("camera", 5, 3);

        stockItem.release(2);

        assertEquals(1, stockItem.held());
        assertEquals(4, stockItem.available());
    }

    @Test
    void shouldRejectReleaseGreaterThanHeldQuantity() {
        StockItem stockItem = new StockItem("camera", 5, 1);

        assertThrows(
                IllegalStateException.class,
                () -> stockItem.release(2)
        );

        assertEquals(1, stockItem.held());
    }

    @Test
    void shouldRejectNonPositiveQuantity() {
        StockItem stockItem = new StockItem("camera", 5, 0);

        assertThrows(
                IllegalArgumentException.class,
                () -> stockItem.tryHold(0)
        );
    }

    @Test
    void shouldRejectInvalidInitialState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new StockItem("camera", 5, 6)
        );
    }
}