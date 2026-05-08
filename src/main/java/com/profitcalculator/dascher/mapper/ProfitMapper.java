package com.profitcalculator.dascher.mapper;

import com.profitcalculator.dascher.dto.ProfitDTO;
import com.profitcalculator.dascher.entity.Shipment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Converts a {@link Shipment} entity and already-computed totals into a {@link ProfitDTO}.
 *
 * <p>Profit/loss calculation logic is always in {@link
 * com.profitcalculator.dascher.util.ProfitCalculator}.
 */
public final class ProfitMapper {

  private ProfitMapper() {}

  // calculatedAt is passed by the caller so the mapper stays deterministic — a mapper that reads
  // the clock breaks time-sensitive tests.
  /** Builds a {@link ProfitDTO} from a {@link Shipment} and already computed totals. */
  public static ProfitDTO toDto(
      Shipment shipment,
      BigDecimal totalIncome,
      BigDecimal totalCosts,
      LocalDateTime calculatedAt) {
    if (shipment == null) return null;
    return ProfitDTO.builder()
        .id(shipment.getShipmentId())
        .shipmentRef(shipment.getShipmentRef())
        .shipmentDate(shipment.getShipmentDate())
        .totalIncome(totalIncome)
        .totalCosts(totalCosts)
        .profitOrLoss(totalIncome.subtract(totalCosts))
        .calculatedAt(calculatedAt)
        .build();
  }
}
