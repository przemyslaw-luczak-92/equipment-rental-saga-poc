package com.example.equipmentrental.inventory.adapter.in.rest;

import com.example.equipmentrental.inventory.adapter.in.rest.generated.api.EquipmentApi;
import com.example.equipmentrental.inventory.adapter.in.rest.generated.model.EquipmentResponse;
import com.example.equipmentrental.inventory.application.port.in.GetEquipmentUseCase;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EquipmentController implements EquipmentApi {

    private final GetEquipmentUseCase getEquipmentUseCase;

    public EquipmentController(GetEquipmentUseCase getEquipmentUseCase) {
        this.getEquipmentUseCase = getEquipmentUseCase;
    }

    @Override
    public ResponseEntity<EquipmentResponse> getEquipment(String equipmentId) {
        return getEquipmentUseCase
                .findByEquipmentId(equipmentId)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private EquipmentResponse toResponse(StockItem stockItem) {
        return new EquipmentResponse(
                stockItem.equipmentId(),
                stockItem.total(),
                stockItem.held(),
                stockItem.available()
        );
    }
}