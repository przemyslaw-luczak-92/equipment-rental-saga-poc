package com.example.equipmentrental.booking.application.port.in;

import java.util.UUID;

public interface CreateBookingUseCase {

    UUID create(String customerId, String equipmentId, int quantity);

}