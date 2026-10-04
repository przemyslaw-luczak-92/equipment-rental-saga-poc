package com.example.equipmentrental.inventory.application.service;

import com.example.equipmentrental.inventory.application.port.in.ReserveStockResult;
import com.example.equipmentrental.inventory.application.port.in.ReserveStockUseCase;
import com.example.equipmentrental.inventory.application.port.out.InventoryHoldRepository;
import com.example.equipmentrental.inventory.application.port.out.StockItemRepository;
import com.example.equipmentrental.inventory.domain.InventoryHold;
import com.example.equipmentrental.inventory.domain.InventoryHoldRefusalReason;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReserveStockService implements ReserveStockUseCase {

    private final StockItemRepository stockItemRepository;
    private final InventoryHoldRepository inventoryHoldRepository;

    public ReserveStockService(StockItemRepository stockItemRepository, InventoryHoldRepository inventoryHoldRepository) {
        this.stockItemRepository = stockItemRepository;
        this.inventoryHoldRepository = inventoryHoldRepository;
    }

    @Override
    public ReserveStockResult reserve(UUID bookingId, String equipmentId, int quantity) {
        Optional<InventoryHold> existingHold = inventoryHoldRepository.findByBookingIdForUpdate(bookingId);

        if (existingHold.isPresent()) {
            return resultForExistingHold(
                    existingHold.get(),
                    equipmentId,
                    quantity
            );
        }

        Optional<StockItem> stockItem = stockItemRepository.findByEquipmentIdForUpdate(equipmentId);

        if (stockItem.isEmpty()) {
            inventoryHoldRepository.save(
                    InventoryHold.refused(
                            bookingId,
                            equipmentId,
                            quantity,
                            InventoryHoldRefusalReason.UNKNOWN_EQUIPMENT
                    )
            );

            return ReserveStockResult.UNKNOWN_EQUIPMENT;
        }

        StockItem item = stockItem.get();

        if (!item.tryHold(quantity)) {
            inventoryHoldRepository.save(
                    InventoryHold.refused(
                            bookingId,
                            equipmentId,
                            quantity,
                            InventoryHoldRefusalReason.INSUFFICIENT_STOCK
                    )
            );

            return ReserveStockResult.INSUFFICIENT_STOCK;
        }

        stockItemRepository.save(item);

        inventoryHoldRepository.save(
                InventoryHold.held(
                        bookingId,
                        equipmentId,
                        quantity
                )
        );

        return ReserveStockResult.RESERVED;
    }

    private ReserveStockResult resultForExistingHold(InventoryHold hold, String equipmentId, int quantity) {
        if (!hold.matches(equipmentId, quantity)) {
            return ReserveStockResult.HOLD_CONFLICT;
        }

        return switch (hold.status()) {
            case HELD, RELEASED -> ReserveStockResult.RESERVED;

            case REFUSED -> switch (hold.refusalReason().orElseThrow()) {
                case UNKNOWN_EQUIPMENT -> ReserveStockResult.UNKNOWN_EQUIPMENT;
                case INSUFFICIENT_STOCK -> ReserveStockResult.INSUFFICIENT_STOCK;
            };
        };
    }
}