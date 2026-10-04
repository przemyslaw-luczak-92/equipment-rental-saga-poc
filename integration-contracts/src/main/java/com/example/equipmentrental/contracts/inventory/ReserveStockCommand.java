package com.example.equipmentrental.contracts.inventory;

import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record ReserveStockCommand(
        UUID bookingId,
        String equipmentId,
        int quantity
) implements Command {
}