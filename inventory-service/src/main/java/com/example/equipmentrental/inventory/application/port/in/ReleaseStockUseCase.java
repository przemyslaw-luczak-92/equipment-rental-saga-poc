package com.example.equipmentrental.inventory.application.port.in;

import java.util.UUID;

public interface ReleaseStockUseCase {

    ReleaseStockResult release(UUID bookingId);

}