package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.RejectBookingResult;
import com.example.equipmentrental.booking.application.port.in.RejectBookingUseCase;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class RejectBookingService implements RejectBookingUseCase {

    private final BookingRepository bookingRepository;

    public RejectBookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public RejectBookingResult reject(UUID bookingId, BookingRejectionReason reason) {
        Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );
        Objects.requireNonNull(
                reason,
                "reason must not be null"
        );

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() -> new IllegalStateException(
                        "Booking does not exist: " + bookingId
                ));

        if (!booking.reject(reason)) {
            return RejectBookingResult.ALREADY_REJECTED;
        }

        bookingRepository.save(booking);

        return RejectBookingResult.REJECTED;
    }
}