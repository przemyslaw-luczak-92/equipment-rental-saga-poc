package com.example.equipmentrental.contracts.booking;

public record ConfirmBookingFailedReply(
        Reason reason
) {

    public enum Reason {
        CUSTOMER_LIMIT_EXCEEDED
    }
}