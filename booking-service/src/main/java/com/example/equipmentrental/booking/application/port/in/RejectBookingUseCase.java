package com.example.equipmentrental.booking.application.port.in;

import com.example.equipmentrental.booking.domain.BookingRejectionReason;

import java.util.UUID;

public interface RejectBookingUseCase {

    RejectBookingResult reject(UUID bookingId, BookingRejectionReason reason);

}