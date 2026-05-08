package com.profitcalculator.dascher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.profitcalculator.dascher.dto.CostDTO;
import com.profitcalculator.dascher.dto.IncomeDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import com.profitcalculator.dascher.entity.CostType;
import com.profitcalculator.dascher.entity.Shipment;
import com.profitcalculator.dascher.exception.ResourceNotFoundException;
import com.profitcalculator.dascher.repository.CostTypeRepository;
import com.profitcalculator.dascher.repository.ShipmentRepository;
import com.profitcalculator.dascher.service.impl.ShipmentServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceImplTest {

  @Mock private ShipmentRepository shipmentRepository;
  @Mock private CostTypeRepository costTypeRepository;
  @InjectMocks private ShipmentServiceImpl shipmentService;

  // ── helpers ──────────────────────────────────────────────────────────────

  private Shipment shipmentEntity() {
    return Shipment.builder()
        .shipmentId(1L)
        .shipmentRef("SHP-TEST-001")
        .description("Test shipment")
        .shipmentDate(LocalDate.of(2026, 4, 29))
        .build();
  }

  private ShipmentDTO shipmentDtoWithItems() {
    return ShipmentDTO.builder()
        .shipmentRef("SHP-TEST-001")
        .description("Test shipment")
        .shipmentDate(LocalDate.of(2026, 4, 29))
        .incomes(List.of(IncomeDTO.builder().amount(new BigDecimal("5000")).build()))
        .costs(
            List.of(
                CostDTO.builder()
                    .amount(new BigDecimal("1000"))
                    .additionalCost(BigDecimal.ZERO)
                    .costTypeName("FUEL")
                    .build()))
        .build();
  }

  // ── findById ─────────────────────────────────────────────────────────────

  @Test
  void findById_whenExists_returnsMappedDto() {
    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipmentEntity()));

    ShipmentDTO result = shipmentService.findById(1L);

    assertThat(result.getShipmentRef()).isEqualTo("SHP-TEST-001");
    assertThat(result.getId()).isEqualTo(1L);
  }

  @Test
  void findById_whenNotFound_throwsResourceNotFoundException() {
    when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("99");
  }

  // ── create ───────────────────────────────────────────────────────────────

  @Test
  void create_withValidDto_savesAndReturnsMappedDto() {
    CostType fuelType = CostType.builder().costTypeId(1L).typeName("FUEL").build();
    when(costTypeRepository.findByTypeName("FUEL")).thenReturn(Optional.of(fuelType));
    when(shipmentRepository.save(any(Shipment.class)))
        .thenAnswer(
            inv -> {
              Shipment s = inv.getArgument(0);
              s.setShipmentId(1L);
              return s;
            });

    ShipmentDTO result = shipmentService.create(shipmentDtoWithItems());

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getShipmentRef()).isEqualTo("SHP-TEST-001");
    assertThat(result.getIncomes()).hasSize(1);
    assertThat(result.getCosts()).hasSize(1);
    verify(shipmentRepository).save(any(Shipment.class));
  }

  @Test
  void create_withUnknownCostType_throwsResourceNotFoundException() {
    IncomeDTO income = IncomeDTO.builder().amount(new BigDecimal("3000")).build();
    CostDTO cost =
        CostDTO.builder()
            .amount(new BigDecimal("500"))
            .additionalCost(BigDecimal.ZERO)
            .costTypeName("UNKNOWN")
            .build();
    ShipmentDTO dto =
        ShipmentDTO.builder()
            .shipmentRef("SHP-ERR-001")
            .shipmentDate(LocalDate.now())
            .incomes(List.of(income))
            .costs(List.of(cost))
            .build();

    when(costTypeRepository.findByTypeName("UNKNOWN")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.create(dto))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("UNKNOWN");
  }

  // ── update ───────────────────────────────────────────────────────────────

  @Test
  void update_replacesFieldsAndReturnsDto() {
    Shipment existing = shipmentEntity();
    ShipmentDTO updateDto =
        ShipmentDTO.builder()
            .shipmentRef("SHP-TEST-001-UPDATED")
            .description("Updated description")
            .shipmentDate(LocalDate.of(2026, 5, 1))
            .build();

    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));

    ShipmentDTO result = shipmentService.update(1L, updateDto);

    assertThat(result.getShipmentRef()).isEqualTo("SHP-TEST-001-UPDATED");
    assertThat(result.getDescription()).isEqualTo("Updated description");
    verify(shipmentRepository).save(existing);
  }

  // ── delete ───────────────────────────────────────────────────────────────

  @Test
  void delete_whenExists_callsRepositoryDelete() {
    Shipment existing = shipmentEntity();
    when(shipmentRepository.findById(1L)).thenReturn(Optional.of(existing));

    shipmentService.delete(1L);

    verify(shipmentRepository).delete(existing);
  }

  @Test
  void delete_whenNotFound_throwsResourceNotFoundException() {
    when(shipmentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shipmentService.delete(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
