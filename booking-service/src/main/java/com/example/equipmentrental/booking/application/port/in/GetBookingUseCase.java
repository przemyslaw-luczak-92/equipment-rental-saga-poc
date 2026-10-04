package com.example.equipmentrental.booking.application.port.in;

import com.example.equipmentrental.booking.domain.Booking;

import java.util.Optional;
import java.util.UUID;

public interface GetBookingUseCase {

    Optional<Booking> findById(UUID bookingId);
}