package com.example.equipmentrental.booking.domain;

public final class CustomerQuota {

    private final String customerId;
    private final int limit;
    private int confirmedCount;

    public CustomerQuota(String customerId, int limit, int confirmedCount) {

        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException(
                    "customerId must not be blank"
            );
        }
        if (limit < 0) {
            throw new IllegalArgumentException(
                    "limit must not be negative"
            );
        }
        if (confirmedCount < 0 || confirmedCount > limit) {
            throw new IllegalArgumentException(
                    "confirmedCount must be between zero and limit"
            );
        }

        this.customerId = customerId;
        this.limit = limit;
        this.confirmedCount = confirmedCount;
    }

    public boolean tryConfirmAnotherBooking() {
        if (confirmedCount >= limit) {
            return false;
        }

        confirmedCount++;
        return true;
    }

    public String customerId() {
        return customerId;
    }

    public int limit() {
        return limit;
    }

    public int confirmedCount() {
        return confirmedCount;
    }
}