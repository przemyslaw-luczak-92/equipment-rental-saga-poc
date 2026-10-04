package com.example.equipmentrental.booking.domain;

public enum BookingRejectionReason {
    UNKNOWN_EQUIPMENT,
    INSUFFICIENT_STOCK,
    HOLD_CONFLICT,
    CUSTOMER_LIMIT_EXCEEDED
}