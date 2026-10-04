package com.example.equipmentrental.contracts.booking;

import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record RejectBookingCommand(
        UUID bookingId,
        Reason reason
) implements Command {

    public enum Reason {
        UNKNOWN_EQUIPMENT,
        INSUFFICIENT_STOCK,
        HOLD_CONFLICT,
        CUSTOMER_LIMIT_EXCEEDED
    }
}