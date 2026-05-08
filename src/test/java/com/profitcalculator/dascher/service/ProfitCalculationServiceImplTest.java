package com.profitcalculator.dascher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.profitcalculator.dascher.dto.ProfitDTO;
import com.profitcalculator.dascher.entity.Cost;
import com.profitcalculator.dascher.entity.CostType;
import com.profitcalculator.dascher.entity.Income;
import com.profitcalculator.dascher.entity.Shipment;
import com.profitcalculator.dascher.exception.ResourceNotFoundException;
import com.profitcalculator.dascher.repository.ShipmentRepository;
import com.profitcalculator.dascher.service.impl.ProfitCalculationServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProfitCalculationServiceImplTest {

  @Mock private ShipmentRepository shipmentRepository;
  @InjectMocks private ProfitCalculationServiceImpl profitService;

  // ── helpers ──────────────────────────────────────────────────────────────

  private Shipment shipment(long id, String ref, List<Income> incomes, List<Cost> costs) {
    return Shipment.builder()
        .shipmentId(id)
        .shipmentRef(ref)
        .shipmentDate(LocalDate.of(2026, 4, 10))
        .incomes(incomes)
        .costs(costs)
        .build();
  }

  private Income income(BigDecimal amount) {
    return Income.builder().amount(amount).build();
  }

  private Cost cost(BigDecimal amount, BigDecimal additional) {
    return Cost.builder()
        .amount(amount)
        .additionalCost(additional)
        .costType(CostType.builder().typeName("FUEL").build())
        .build();
  }

  // ── calculate ─────────────────────────────────────────────────────────────

  @Test
  void calculate_whenFound_returnsMappedProfitDto() {
    Shipment s =
        shipment(
            101L,
            "SHP-2026-001",
            List.of(income(new BigDecimal("5200"))),
            List.of(cost(new BigDecimal("2000"), new BigDecimal("125"))));

    when(shipmentRepository.findById(101L)).thenReturn(Optional.of(s));
    when(shipmentRepository.save(any(Shipment.class))).thenReturn(s);

    ProfitDTO result = profitService.calculate(101L);

    assertThat(result.getShipmentRef()).isEqualTo("SHP-2026-001");
    assertThat(result.getTotalIncome()).isEqualByComparingTo("5200");
    assertThat(result.getTotalCosts()).isEqualByComparingTo("2125");
    assertThat(result.getProfitOrLoss()).isEqualByComparingTo("3075");
    assertThat(result.getCalculatedAt()).isNotNull();
  }

  @Test
  void calculate_whenNotFound_throwsResourceNotFoundException() {
    when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> profitService.calculate(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("999");
  }

  @Test
  void calculate_whenLoss_returnsNegativeProfitOrLoss() {
    Shipment s =
        shipment(
            102L,
            "SHP-2026-002",
            List.of(income(new BigDecimal("500"))),
            List.of(cost(new BigDecimal("1000"), BigDecimal.ZERO)));

    when(shipmentRepository.findById(102L)).thenReturn(Optional.of(s));
    when(shipmentRepository.save(any(Shipment.class))).thenReturn(s);

    ProfitDTO result = profitService.calculate(102L);

    assertThat(result.getProfitOrLoss()).isEqualByComparingTo("-500");
  }

  // ── findAll ───────────────────────────────────────────────────────────────

  @Test
  void findAll_returnsPageOfMappedDtos() {
    PageRequest pageable = PageRequest.of(0, 10);

    Shipment s1 =
        shipment(
            101L,
            "SHP-2026-001",
            List.of(income(new BigDecimal("5200"))),
            List.of(cost(new BigDecimal("2000"), new BigDecimal("125"))));

    Shipment s2 =
        shipment(
            102L,
            "SHP-2026-002",
            List.of(income(new BigDecimal("4300"))),
            List.of(cost(new BigDecimal("1800"), BigDecimal.ZERO)));

    when(shipmentRepository.findAll(pageable))
        .thenReturn(new PageImpl<>(List.of(s1, s2), pageable, 2));

    Page<ProfitDTO> result = profitService.findAll(pageable);

    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent().get(0).getProfitOrLoss()).isEqualByComparingTo("3075");
    assertThat(result.getContent().get(1).getProfitOrLoss()).isEqualByComparingTo("2500");
  }
}
