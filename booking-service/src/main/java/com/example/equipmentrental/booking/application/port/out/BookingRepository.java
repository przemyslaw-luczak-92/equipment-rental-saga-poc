package com.example.equipmentrental.booking.application.port.out;

import com.example.equipmentrental.booking.domain.Booking;

import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {

    Optional<Booking> findById(UUID bookingId);

    Optional<Booking> findByIdForUpdate(UUID bookingId);

    void save(Booking booking);
}