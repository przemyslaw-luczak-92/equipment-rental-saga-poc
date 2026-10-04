package com.example.equipmentrental.inventory.adapter.out.persistence;

import com.example.equipmentrental.inventory.application.port.out.StockItemRepository;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class StockItemPersistenceAdapter implements StockItemRepository {

    private final SpringDataStockItemRepository repository;

    public StockItemPersistenceAdapter(SpringDataStockItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StockItem> findByEquipmentId(String equipmentId) {
        return repository
                .findById(equipmentId)
                .map(this::toDomain);
    }

    @Override
    public Optional<StockItem> findByEquipmentIdForUpdate(String equipmentId) {
        return repository
                .findByEquipmentIdForUpdate(equipmentId)
                .map(this::toDomain);
    }

    @Override
    public void save(StockItem stockItem) {
        repository.save(toEntity(stockItem));
    }

    private StockItem toDomain(StockItemJpaEntity entity) {
        return new StockItem(
                entity.getEquipmentId(),
                entity.getTotal(),
                entity.getHeld()
        );
    }

    private StockItemJpaEntity toEntity(StockItem stockItem) {
        return new StockItemJpaEntity(
                stockItem.equipmentId(),
                stockItem.total(),
                stockItem.held()
        );
    }
}