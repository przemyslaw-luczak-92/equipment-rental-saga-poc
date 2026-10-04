package com.example.equipmentrental.contracts.booking;

import io.eventuate.tram.commands.common.Command;

import java.util.UUID;

public record ConfirmBookingCommand(
        UUID bookingId
) implements Command {
}