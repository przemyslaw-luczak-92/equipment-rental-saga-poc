package com.example.equipmentrental.booking.adapter.in.rest;

import com.example.equipmentrental.booking.application.port.in.CreateBookingUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.equipmentrental.booking.application.port.in.GetBookingUseCase;
import com.example.equipmentrental.booking.domain.Booking;
import com.example.equipmentrental.booking.domain.BookingRejectionReason;
import com.example.equipmentrental.booking.domain.BookingStatus;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class BookingControllerTest {

    private static final UUID BOOKING_ID = UUID.fromString("c30b4e31-6b63-4c70-bcb6-10f214545671");

    private FakeCreateBookingUseCase useCase;
    private FakeGetBookingUseCase getBookingUseCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = new FakeCreateBookingUseCase();
        getBookingUseCase = new FakeGetBookingUseCase();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new BookingController(useCase, getBookingUseCase)
                )
                .build();
    }

    @Test
    void shouldAcceptBookingCreation() throws Exception {
        mockMvc.perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "customer-normal",
                                          "equipmentId": "camera",
                                          "quantity": 2
                                        }
                                        """)
                )
                .andExpect(status().isAccepted())
                .andExpect(
                        header().string(
                                "Location",
                                "/bookings/" + BOOKING_ID
                        )
                )
                .andExpect(
                        jsonPath("$.bookingId")
                                .value(BOOKING_ID.toString())
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("PENDING")
                );

        assertEquals(
                "customer-normal",
                useCase.customerId
        );
        assertEquals(
                "camera",
                useCase.equipmentId
        );
        assertEquals(2, useCase.quantity);
    }

    @Test
    void shouldRejectInvalidQuantity() throws Exception {
        mockMvc.perform(
                        post("/bookings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "customerId": "customer-normal",
                                          "equipmentId": "camera",
                                          "quantity": 0
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest());

        assertFalse(useCase.called);
    }

    @Test
    void shouldReturnBooking() throws Exception {
        getBookingUseCase.booking = Optional.of(
                Booking.restore(
                        BOOKING_ID,
                        "customer-normal",
                        "camera",
                        4,
                        BookingStatus.REJECTED,
                        BookingRejectionReason.INSUFFICIENT_STOCK
                )
        );

        mockMvc.perform(get("/bookings/{bookingId}", BOOKING_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(BOOKING_ID.toString()))
                .andExpect(jsonPath("$.customerId").value("customer-normal"))
                .andExpect(jsonPath("$.equipmentId").value("camera"))
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(
                        jsonPath("$.rejectionReason")
                                .value("INSUFFICIENT_STOCK")
                );

        assertEquals(
                BOOKING_ID,
                getBookingUseCase.requestedBookingId
        );
    }

    @Test
    void shouldReturnNotFoundForUnknownBooking() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", BOOKING_ID))
                .andExpect(status().isNotFound());

        assertEquals(
                BOOKING_ID,
                getBookingUseCase.requestedBookingId
        );
    }

    private static final class FakeCreateBookingUseCase implements CreateBookingUseCase {

        private boolean called;
        private String customerId;
        private String equipmentId;
        private int quantity;

        @Override
        public UUID create(String customerId, String equipmentId, int quantity) {
            called = true;
            this.customerId = customerId;
            this.equipmentId = equipmentId;
            this.quantity = quantity;

            return BOOKING_ID;
        }
    }

    private static final class FakeGetBookingUseCase implements GetBookingUseCase {

        private UUID requestedBookingId;
        private Optional<Booking> booking = Optional.empty();

        @Override
        public Optional<Booking> findById(UUID bookingId) {
            requestedBookingId = bookingId;
            return booking;
        }
    }
}