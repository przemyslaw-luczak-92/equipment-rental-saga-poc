package com.example.equipmentrental.inventory.application.service;

import com.example.equipmentrental.inventory.application.port.in.ReleaseStockResult;
import com.example.equipmentrental.inventory.application.port.in.ReleaseStockUseCase;
import com.example.equipmentrental.inventory.application.port.out.InventoryHoldRepository;
import com.example.equipmentrental.inventory.application.port.out.StockItemRepository;
import com.example.equipmentrental.inventory.domain.InventoryHold;
import com.example.equipmentrental.inventory.domain.InventoryHoldStatus;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ReleaseStockService implements ReleaseStockUseCase {

    private final StockItemRepository stockItemRepository;
    private final InventoryHoldRepository inventoryHoldRepository;

    public ReleaseStockService(StockItemRepository stockItemRepository, InventoryHoldRepository inventoryHoldRepository) {
        this.stockItemRepository = stockItemRepository;
        this.inventoryHoldRepository = inventoryHoldRepository;
    }

    @Override
    public ReleaseStockResult release(UUID bookingId) {
        Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        Optional<InventoryHold> existingHold = inventoryHoldRepository.findByBookingIdForUpdate(bookingId);

        if (existingHold.isEmpty()) {
            return ReleaseStockResult.NOTHING_TO_RELEASE;
        }

        InventoryHold hold = existingHold.get();

        if (hold.status() == InventoryHoldStatus.RELEASED) {
            return ReleaseStockResult.ALREADY_RELEASED;
        }

        if (hold.status() == InventoryHoldStatus.REFUSED) {
            return ReleaseStockResult.NOTHING_TO_RELEASE;
        }

        StockItem stockItem = stockItemRepository
                .findByEquipmentIdForUpdate(hold.equipmentId())
                .orElseThrow(() -> new IllegalStateException(
                        "Stock item for an active hold does not exist"
                ));

        stockItem.release(hold.quantity());

        if (!hold.release()) {
            throw new IllegalStateException(
                    "Active inventory hold could not be released"
            );
        }

        stockItemRepository.save(stockItem);
        inventoryHoldRepository.save(hold);

        return ReleaseStockResult.RELEASED;
    }
}