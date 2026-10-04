package com.example.equipmentrental.booking.application.service;

import com.example.equipmentrental.booking.application.port.in.CreateBookingUseCase;
import com.example.equipmentrental.booking.application.port.out.BookingRepository;
import com.example.equipmentrental.booking.application.port.out.CreateBookingSagaStarter;
import com.example.equipmentrental.booking.domain.Booking;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CreateBookingService implements CreateBookingUseCase {

    private final BookingRepository bookingRepository;
    private final CreateBookingSagaStarter sagaStarter;

    public CreateBookingService(BookingRepository bookingRepository, CreateBookingSagaStarter sagaStarter) {
        this.bookingRepository = bookingRepository;
        this.sagaStarter = sagaStarter;
    }

    @Override
    public UUID create(String customerId, String equipmentId, int quantity) {
        UUID bookingId = UUID.randomUUID();

        Booking booking = Booking.pending(
                bookingId,
                customerId,
                equipmentId,
                quantity
        );

        bookingRepository.save(booking);
        sagaStarter.start(booking);

        return bookingId;
    }
}