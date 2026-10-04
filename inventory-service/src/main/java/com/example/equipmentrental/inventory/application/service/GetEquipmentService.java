package com.example.equipmentrental.inventory.application.service;

import com.example.equipmentrental.inventory.application.port.in.GetEquipmentUseCase;
import com.example.equipmentrental.inventory.application.port.out.StockItemRepository;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class GetEquipmentService implements GetEquipmentUseCase {

    private final StockItemRepository stockItemRepository;

    public GetEquipmentService(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    @Override
    public Optional<StockItem> findByEquipmentId(String equipmentId) {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException(
                    "equipmentId must not be blank"
            );
        }

        return stockItemRepository.findByEquipmentId(equipmentId);
    }
}