package com.example.equipmentrental.inventory.application.service;

import com.example.equipmentrental.inventory.application.port.in.ReserveStockResult;
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

class ReserveStockServiceTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();

    private FakeStockItemRepository stockItems;
    private FakeInventoryHoldRepository holds;
    private ReserveStockService service;

    @BeforeEach
    void setUp() {
        stockItems = new FakeStockItemRepository();
        holds = new FakeInventoryHoldRepository();
        service = new ReserveStockService(stockItems, holds);
    }

    @Test
    void shouldReserveAvailableStock() {
        stockItems.current = new StockItem("camera", 5, 0);

        ReserveStockResult result = service.reserve(BOOKING_ID, "camera", 2);

        assertEquals(ReserveStockResult.RESERVED, result);
        assertEquals(2, stockItems.saved.held());
        assertEquals(InventoryHoldStatus.HELD, holds.saved.status());
    }

    @Test
    void shouldRefuseUnknownEquipment() {
        ReserveStockResult result = service.reserve(BOOKING_ID, "unknown", 2);

        assertEquals(ReserveStockResult.UNKNOWN_EQUIPMENT, result);
        assertEquals(InventoryHoldStatus.REFUSED, holds.saved.status());
        assertEquals(InventoryHoldRefusalReason.UNKNOWN_EQUIPMENT, holds.saved.refusalReason().orElseThrow());
    }

    @Test
    void shouldRefuseWhenStockIsInsufficient() {
        stockItems.current = new StockItem("camera", 1, 0);

        ReserveStockResult result = service.reserve(BOOKING_ID, "camera", 2);

        assertEquals(ReserveStockResult.INSUFFICIENT_STOCK, result);
        assertEquals(0, stockItems.current.held());
        assertNull(stockItems.saved);
        assertEquals(InventoryHoldRefusalReason.INSUFFICIENT_STOCK, holds.saved.refusalReason().orElseThrow());
    }

    @Test
    void shouldReturnPreviousSuccessfulResult() {
        holds.current = InventoryHold.held(
                BOOKING_ID,
                "camera",
                2
        );

        ReserveStockResult result = service.reserve(BOOKING_ID, "camera", 2);

        assertEquals(ReserveStockResult.RESERVED, result);
        assertNull(stockItems.saved);
        assertNull(holds.saved);
    }

    @Test
    void shouldDetectConflictingRequest() {
        holds.current = InventoryHold.held(
                BOOKING_ID,
                "camera",
                2
        );

        ReserveStockResult result = service.reserve(BOOKING_ID, "projector", 2);

        assertEquals(ReserveStockResult.HOLD_CONFLICT, result);
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