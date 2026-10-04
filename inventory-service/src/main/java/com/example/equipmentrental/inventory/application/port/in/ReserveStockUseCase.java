package com.example.equipmentrental.inventory.application.port.in;

import java.util.UUID;

public interface ReserveStockUseCase {

    ReserveStockResult reserve(UUID bookingId, String equipmentId, int quantity);

}