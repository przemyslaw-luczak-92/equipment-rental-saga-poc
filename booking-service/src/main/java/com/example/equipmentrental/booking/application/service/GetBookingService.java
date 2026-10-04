package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.GetBookingUseCase;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.domain.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetBookingService implements GetBookingUseCase {

    private final BookingRepository bookingRepository;

    public GetBookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Optional<Booking> findById(UUID bookingId) {
        Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        return bookingRepository.findById(bookingId);
    }
}