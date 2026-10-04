package com.example.equipmentrental.contracts.inventory;

import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record ReleaseStockCommand(
        UUID bookingId
) implements Command {
}