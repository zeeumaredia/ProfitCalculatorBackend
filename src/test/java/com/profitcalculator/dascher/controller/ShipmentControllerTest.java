package com.profitcalculator.dascher.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.profitcalculator.dascher.dto.ProfitDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import com.profitcalculator.dascher.exception.ResourceNotFoundException;
import com.profitcalculator.dascher.service.ProfitCalculationService;
import com.profitcalculator.dascher.service.ShipmentService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    value = ShipmentController.class,
    excludeAutoConfiguration = {
      SecurityAutoConfiguration.class,
      SecurityFilterAutoConfiguration.class
    })
class ShipmentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @MockBean private ShipmentService shipmentService;
  @MockBean private ProfitCalculationService profitCalculationService;

  // ── helpers ───────────────────────────────────────────────────────────────

  private ShipmentDTO sampleShipment() {
    return ShipmentDTO.builder()
        .id(101L)
        .shipmentRef("SHP-2026-001")
        .description("Electronics shipment from London to Berlin")
        .shipmentDate(LocalDate.of(2026, 4, 10))
        .incomes(List.of())
        .costs(List.of())
        .build();
  }

  private ProfitDTO sampleProfit() {
    return ProfitDTO.builder()
        .id(101L)
        .shipmentRef("SHP-2026-001")
        .shipmentDate(LocalDate.of(2026, 4, 10))
        .totalIncome(new BigDecimal("5200.00"))
        .totalCosts(new BigDecimal("2125.00"))
        .profitOrLoss(new BigDecimal("3075.00"))
        .calculatedAt(LocalDateTime.now())
        .build();
  }

  // ── GET /api/shipments ────────────────────────────────────────────────────

  @Test
  void findAll_returns200_withPagedShipments() throws Exception {
    Page<ShipmentDTO> page = new PageImpl<>(List.of(sampleShipment()), PageRequest.of(0, 10), 1);
    given(shipmentService.findAll(any())).willReturn(page);

    mockMvc
        .perform(get("/api/shipments").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.data.content[0].shipmentRef").value("SHP-2026-001"))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  // ── GET /api/shipments/{id} ───────────────────────────────────────────────

  @Test
  void findById_returns200_whenExists() throws Exception {
    given(shipmentService.findById(101L)).willReturn(sampleShipment());

    mockMvc
        .perform(get("/api/shipments/101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.shipmentRef").value("SHP-2026-001"))
        .andExpect(jsonPath("$.data.id").value(101));
  }

  @Test
  void findById_returns404_whenNotFound() throws Exception {
    given(shipmentService.findById(999L))
        .willThrow(new ResourceNotFoundException("Shipment not found with id: 999"));

    mockMvc
        .perform(get("/api/shipments/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found with id: 999"));
  }

  // ── POST /api/shipments ───────────────────────────────────────────────────

  @Test
  void create_returns201_withValidBody() throws Exception {
    ShipmentDTO incoming =
        ShipmentDTO.builder()
            .shipmentRef("SHP-NEW-001")
            .description("New shipment")
            .shipmentDate(LocalDate.of(2026, 5, 1))
            .build();

    ShipmentDTO saved =
        ShipmentDTO.builder()
            .id(105L)
            .shipmentRef("SHP-NEW-001")
            .description("New shipment")
            .shipmentDate(LocalDate.of(2026, 5, 1))
            .build();

    given(shipmentService.create(any(ShipmentDTO.class))).willReturn(saved);

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.id").value(105))
        .andExpect(jsonPath("$.data.shipmentRef").value("SHP-NEW-001"));
  }

  @Test
  void create_returns400_whenShipmentRefIsBlank() throws Exception {
    ShipmentDTO invalid =
        ShipmentDTO.builder().shipmentRef("").shipmentDate(LocalDate.now()).build();

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.errors.shipmentRef").exists());
  }

  @Test
  void create_returns400_whenShipmentDateIsNull() throws Exception {
    ShipmentDTO invalid = ShipmentDTO.builder().shipmentRef("SHP-NEW-002").build();

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Bad Request"))
        .andExpect(jsonPath("$.errors.shipmentDate").exists());
  }

  @Test
  void create_returns409_whenShipmentRefIsDuplicate() throws Exception {
    ShipmentDTO incoming =
        ShipmentDTO.builder()
            .shipmentRef("SHP-2026-001")
            .shipmentDate(LocalDate.of(2026, 5, 1))
            .build();

    given(shipmentService.create(any(ShipmentDTO.class)))
        .willThrow(new DataIntegrityViolationException("Duplicate entry for shipment_ref"));

    mockMvc
        .perform(
            post("/api/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(incoming)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.title").value("Conflict"))
        .andExpect(
            jsonPath("$.detail").value("A record with the same unique value already exists."));
  }

  // ── PUT /api/shipments/{id} ───────────────────────────────────────────────

  @Test
  void update_returns200_withUpdatedDto() throws Exception {
    ShipmentDTO updateDto =
        ShipmentDTO.builder()
            .shipmentRef("SHP-2026-001")
            .description("Updated description")
            .shipmentDate(LocalDate.of(2026, 4, 10))
            .build();

    ShipmentDTO updated =
        ShipmentDTO.builder()
            .id(101L)
            .shipmentRef("SHP-2026-001")
            .description("Updated description")
            .shipmentDate(LocalDate.of(2026, 4, 10))
            .build();

    given(shipmentService.update(eq(101L), any(ShipmentDTO.class))).willReturn(updated);

    mockMvc
        .perform(
            put("/api/shipments/101")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.description").value("Updated description"));
  }

  // ── DELETE /api/shipments/{id} ────────────────────────────────────────────

  @Test
  void delete_returns200_withDeletionMessage() throws Exception {
    willDoNothing().given(shipmentService).delete(101L);

    mockMvc
        .perform(delete("/api/shipments/101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void delete_returns404_whenShipmentNotFound() throws Exception {
    willThrow(new ResourceNotFoundException("Shipment not found with id: 999"))
        .given(shipmentService)
        .delete(999L);

    mockMvc
        .perform(delete("/api/shipments/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource Not Found"));
  }

  // ── GET /api/shipments/profit ─────────────────────────────────────────────

  @Test
  void findAllProfits_returns200_withPagedProfits() throws Exception {
    Page<ProfitDTO> page = new PageImpl<>(List.of(sampleProfit()), PageRequest.of(0, 10), 1);
    given(profitCalculationService.findAll(any())).willReturn(page);

    mockMvc
        .perform(get("/api/shipments/profit").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content[0].shipmentRef").value("SHP-2026-001"))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  // ── GET /api/shipments/{id}/profit ────────────────────────────────────────

  @Test
  void getProfit_returns200_withProfitDto() throws Exception {
    given(profitCalculationService.calculate(101L)).willReturn(sampleProfit());

    mockMvc
        .perform(get("/api/shipments/101/profit"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.shipmentRef").value("SHP-2026-001"))
        .andExpect(jsonPath("$.data.totalIncome").value(5200.00))
        .andExpect(jsonPath("$.data.totalCosts").value(2125.00))
        .andExpect(jsonPath("$.data.profitOrLoss").value(3075.00));
  }

  @Test
  void getProfit_returns404_whenShipmentNotFound() throws Exception {
    given(profitCalculationService.calculate(999L))
        .willThrow(new ResourceNotFoundException("Shipment not found with id: 999"));

    mockMvc
        .perform(get("/api/shipments/999/profit"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.title").value("Resource Not Found"))
        .andExpect(jsonPath("$.detail").value("Shipment not found with id: 999"));
  }
}
