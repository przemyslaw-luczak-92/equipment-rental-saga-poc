package com.example.equipmentrental.booking.adapter.in.rest;

import com.example.equipmentrental.booking.adapter.in.rest.generated.api.BookingsApi;
import com.example.equipmentrental.booking.adapter.in.rest.generated.model.BookingStatus;
import com.example.equipmentrental.booking.adapter.in.rest.generated.model.CreateBookingRequest;
import com.example.equipmentrental.booking.adapter.in.rest.generated.model.CreateBookingResponse;
import com.example.equipmentrental.booking.application.port.in.CreateBookingUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import com.example.equipmentrental.booking.adapter.in.rest.generated.model.BookingRejectionReason;
import com.example.equipmentrental.booking.adapter.in.rest.generated.model.BookingResponse;
import com.example.equipmentrental.booking.application.port.in.GetBookingUseCase;
import com.example.equipmentrental.booking.domain.Booking;

import java.net.URI;
import java.util.UUID;

@RestController
public class BookingController implements BookingsApi {

    private final CreateBookingUseCase createBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;

    public BookingController(CreateBookingUseCase createBookingUseCase, GetBookingUseCase getBookingUseCase) {
        this.createBookingUseCase = createBookingUseCase;
        this.getBookingUseCase = getBookingUseCase;
    }

    @Override
    public ResponseEntity<CreateBookingResponse> createBooking(@Valid CreateBookingRequest request) {
        UUID bookingId = createBookingUseCase.create(
                request.getCustomerId(),
                request.getEquipmentId(),
                request.getQuantity()
        );

        CreateBookingResponse response = new CreateBookingResponse(
                bookingId,
                BookingStatus.PENDING
        );

        URI location = URI.create("/bookings/" + bookingId);

        return ResponseEntity
                .accepted()
                .location(location)
                .body(response);
    }

    @Override
    public ResponseEntity<BookingResponse> getBooking(UUID bookingId) {
        return getBookingUseCase
                .findById(bookingId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse(
                booking.bookingId(),
                booking.customerId(),
                booking.equipmentId(),
                booking.quantity(),
                BookingStatus.valueOf(booking.status().name())
        );

        booking.rejectionReason().ifPresent(reason ->
                response.setRejectionReason(
                        BookingRejectionReason.valueOf(reason.name())
                )
        );

        return response;
    }
}