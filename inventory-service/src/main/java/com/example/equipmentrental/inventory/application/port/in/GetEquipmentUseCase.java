package com.example.equipmentrental.inventory.application.port.in;

import com.example.equipmentrental.inventory.domain.StockItem;

import java.util.Optional;

public interface GetEquipmentUseCase {

    Optional<StockItem> findByEquipmentId(String equipmentId);
}