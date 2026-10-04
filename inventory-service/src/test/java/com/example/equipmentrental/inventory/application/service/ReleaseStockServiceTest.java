package com.example.equipmentrental.inventory.application.service;

import com.example.equipmentrental.inventory.application.port.in.ReleaseStockResult;
import com.example.equipmentrental.inventory.application.port.out.InventoryHoldRepository;
import com.example.equipmentrental.inventory.application.port.out.StockItemRepository;
import com.example.equipmentrental.inventory.domain.InventoryHold;
import com.example.equipmentrental.inventory.domain.InventoryHoldRefusalReason;
import com.example.equipmentrental.inventory.domain.InventoryHoldStatus;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReleaseStockServiceTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    private FakeStockItemRepository stockItems;
    private FakeInventoryHoldRepository holds;
    private ReleaseStockService service;

    @BeforeEach
    void setUp() {
        stockItems = new FakeStockItemRepository();
        holds = new FakeInventoryHoldRepository();
        service = new ReleaseStockService(stockItems, holds);
    }

    @Test
    void shouldReleaseHeldStock() {
        stockItems.current = new StockItem("camera", 5, 3);
        holds.current = InventoryHold.held(
                BOOKING_ID,
                "camera",
                2
        );

        ReleaseStockResult result = service.release(BOOKING_ID);

        assertEquals(ReleaseStockResult.RELEASED, result);
        assertEquals(1, stockItems.saved.held());
        assertEquals(InventoryHoldStatus.RELEASED, holds.saved.status());
    }

    @Test
    void shouldNotReleaseStockTwice() {
        holds.current = InventoryHold.restore(
                BOOKING_ID,
                "camera",
                2,
                InventoryHoldStatus.RELEASED,
                null
        );

        ReleaseStockResult result = service.release(BOOKING_ID);

        assertEquals(ReleaseStockResult.ALREADY_RELEASED, result);
        assertNull(stockItems.saved);
        assertNull(holds.saved);
    }

    @Test
    void shouldIgnoreRefusedHold() {
        holds.current = InventoryHold.refused(
                BOOKING_ID,
                "camera",
                2,
                InventoryHoldRefusalReason.INSUFFICIENT_STOCK
        );

        ReleaseStockResult result = service.release(BOOKING_ID);

        assertEquals(ReleaseStockResult.NOTHING_TO_RELEASE, result);
        assertNull(stockItems.saved);
        assertNull(holds.saved);
    }

    @Test
    void shouldIgnoreMissingHold() {
        ReleaseStockResult result = service.release(BOOKING_ID);

        assertEquals(ReleaseStockResult.NOTHING_TO_RELEASE, result);
        assertNull(stockItems.saved);
        assertNull(holds.saved);
    }

    @Test
    void shouldFailWhenStockForActiveHoldDoesNotExist() {
        holds.current = InventoryHold.held(
                BOOKING_ID,
                "camera",
                2
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.release(BOOKING_ID)
        );

        assertNull(stockItems.saved);
        assertNull(holds.saved);
    }

    private static final class FakeStockItemRepository implements StockItemRepository {

        private StockItem current;
        private StockItem saved;

        @Override
        public Optional<StockItem> findByEquipmentId(String equipmentId) {
            return Optional.ofNullable(current);
        }

        @Override
        public Optional<StockItem> findByEquipmentIdForUpdate(String equipmentId) {
            return Optional.ofNullable(current);
        }

        @Override
        public void save(StockItem stockItem) {
            saved = stockItem;
            current = stockItem;
        }
    }

    private static final class FakeInventoryHoldRepository implements InventoryHoldRepository {

        private InventoryHold current;
        private InventoryHold saved;

        @Override
        public Optional<InventoryHold> findByBookingIdForUpdate(UUID bookingId) {
            return Optional.ofNullable(current);
        }

        @Override
        public void save(InventoryHold inventoryHold) {
            saved = inventoryHold;
            current = inventoryHold;
        }
    }
}