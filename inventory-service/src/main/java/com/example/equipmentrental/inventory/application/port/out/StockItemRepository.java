package com.example.equipmentrental.inventory.application.port.out;

import com.example.equipmentrental.inventory.domain.StockItem;

import java.util.Optional;

public interface StockItemRepository {

    Optional<StockItem> findByEquipmentId(String equipmentId);

    Optional<StockItem> findByEquipmentIdForUpdate(String equipmentId);

    void save(StockItem stockItem);
}