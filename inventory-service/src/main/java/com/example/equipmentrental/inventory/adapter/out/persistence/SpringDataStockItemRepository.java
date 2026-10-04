package com.example.equipmentrental.inventory.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SpringDataStockItemRepository
        extends JpaRepository<StockItemJpaEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select stockItem
            from StockItemJpaEntity stockItem
            where stockItem.equipmentId = :equipmentId
            """)
    Optional<StockItemJpaEntity> findByEquipmentIdForUpdate(
            @Param("equipmentId") String equipmentId
    );
}