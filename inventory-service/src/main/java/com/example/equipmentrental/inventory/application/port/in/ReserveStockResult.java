package com.example.equipmentrental.inventory.application.port.in;

public enum ReserveStockResult {
    RESERVED,
    UNKNOWN_EQUIPMENT,
    INSUFFICIENT_STOCK,
    HOLD_CONFLICT
}