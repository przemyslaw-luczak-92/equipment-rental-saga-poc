package com.example.equipmentrental.booking.application.port.in;

import java.util.UUID;

public interface ConfirmBookingUseCase {

    ConfirmBookingResult confirm(UUID bookingId);
}