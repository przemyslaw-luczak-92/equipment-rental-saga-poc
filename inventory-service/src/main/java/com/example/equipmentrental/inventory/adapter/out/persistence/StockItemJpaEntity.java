package com.example.equipmentrental.inventory.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stock_item")
public class StockItemJpaEntity {

    @Id
    @Column(name = "equipment_id", nullable = false, length = 100)
    private String equipmentId;

    @Column(name = "total", nullable = false)
    private int total;

    @Column(name = "held", nullable = false)
    private int held;

    protected StockItemJpaEntity() {
        // Konstruktor wymagany przez JPA.
    }

    public StockItemJpaEntity(String equipmentId, int total, int held) {
        this.equipmentId = equipmentId;
        this.total = total;
        this.held = held;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public int getTotal() {
        return total;
    }

    public int getHeld() {
        return held;
    }
}