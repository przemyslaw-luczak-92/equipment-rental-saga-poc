package com.example.equipmentrental.inventory.domain;

public final class StockItem {

    private final String equipmentId;
    private final int total;
    private int held;

    public StockItem(String equipmentId, int total, int held) {
        if (equipmentId == null || equipmentId.isBlank()) {
            throw new IllegalArgumentException("equipmentId must not be blank");
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must not be negative");
        }
        if (held < 0 || held > total) {
            throw new IllegalArgumentException("held must be between zero and total");
        }

        this.equipmentId = equipmentId;
        this.total = total;
        this.held = held;
    }

    public boolean tryHold(int quantity) {
        requirePositiveQuantity(quantity);

        if (quantity > available()) {
            return false;
        }

        held += quantity;
        return true;
    }

    public void release(int quantity) {
        requirePositiveQuantity(quantity);

        if (quantity > held) {
            throw new IllegalStateException("Cannot release more items than currently held");
        }

        held -= quantity;
    }

    public int available() {
        return total - held;
    }

    public String equipmentId() {
        return equipmentId;
    }

    public int total() {
        return total;
    }

    public int held() {
        return held;
    }

    private static void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }
}