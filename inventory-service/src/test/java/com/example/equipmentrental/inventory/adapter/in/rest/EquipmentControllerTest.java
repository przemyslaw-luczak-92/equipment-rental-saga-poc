package com.example.equipmentrental.inventory.adapter.in.rest;

import com.example.equipmentrental.inventory.application.port.in.GetEquipmentUseCase;
import com.example.equipmentrental.inventory.domain.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EquipmentControllerTest {

    private FakeGetEquipmentUseCase useCase;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        useCase = new FakeGetEquipmentUseCase();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new EquipmentController(useCase))
                .build();
    }

    @Test
    void shouldReturnEquipmentAvailability() throws Exception {
        useCase.stockItem = Optional.of(
                new StockItem("camera", 5, 2)
        );

        mockMvc.perform(get("/equipment/{equipmentId}", "camera"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.equipmentId").value("camera"))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.held").value(2))
                .andExpect(jsonPath("$.available").value(3));

        assertEquals("camera", useCase.requestedEquipmentId);
    }

    @Test
    void shouldReturnNotFoundForUnknownEquipment() throws Exception {
        mockMvc.perform(get("/equipment/{equipmentId}", "projector"))
                .andExpect(status().isNotFound());

        assertEquals("projector", useCase.requestedEquipmentId);
    }

    private static final class FakeGetEquipmentUseCase implements GetEquipmentUseCase {

        private String requestedEquipmentId;
        private Optional<StockItem> stockItem = Optional.empty();

        @Override
        public Optional<StockItem> findByEquipmentId(String equipmentId) {
            requestedEquipmentId = equipmentId;
            return stockItem;
        }
    }
}