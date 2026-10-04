package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.ConfirmBookingResult;
import com.example.equipmentrental.booking.application.port.in.ConfirmBookingUseCase;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.application.port.out.CustomerQuotaRepository;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingStatus;
import com.example.equipmentrental.booking.domain.CustomerQuota;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class ConfirmBookingService implements ConfirmBookingUseCase {

    private final BookingRepository bookingRepository;
    private final CustomerQuotaRepository customerQuotaRepository;

    public ConfirmBookingService(BookingRepository bookingRepository, CustomerQuotaRepository customerQuotaRepository) {
        this.bookingRepository = bookingRepository;
        this.customerQuotaRepository = customerQuotaRepository;
    }

    @Override
    public ConfirmBookingResult confirm(UUID bookingId) {
        Objects.requireNonNull(
                bookingId,
                "bookingId must not be null"
        );

        Booking booking = bookingRepository
                .findByIdForUpdate(bookingId)
                .orElseThrow(() -> new IllegalStateException(
                        "Booking does not exist: " + bookingId
                ));

        if (booking.status() == BookingStatus.CONFIRMED) {
            return ConfirmBookingResult.CONFIRMED;
        }

        if (booking.status() == BookingStatus.REJECTED) {
            throw new IllegalStateException(
                    "Rejected booking cannot be confirmed"
            );
        }

        CustomerQuota customerQuota = customerQuotaRepository
                .findByCustomerIdForUpdate(booking.customerId())
                .orElseThrow(() -> new IllegalStateException(
                        "Customer quota does not exist: "
                                + booking.customerId()
                ));

        if (!customerQuota.tryConfirmAnotherBooking()) {
            return ConfirmBookingResult.CUSTOMER_LIMIT_EXCEEDED;
        }

        if (!booking.confirm()) {
            throw new IllegalStateException(
                    "Pending booking could not be confirmed"
            );
        }

        customerQuotaRepository.save(customerQuota);
        bookingRepository.save(booking);

        return ConfirmBookingResult.CONFIRMED;
    }
}