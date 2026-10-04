package com.example.equipmentrental.contracts.inventory;

public record ReserveStockFailedReply(
        Reason reason
) {

    public enum Reason {
        UNKNOWN_EQUIPMENT,
        INSUFFICIENT_STOCK,
        HOLD_CONFLICT
    }
}