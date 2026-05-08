package com.profitcalculator.dascher.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Read-only result of a profit/loss calculation for a shipment.
 *
 * <p>profitOrLoss = totalIncome − totalCosts. A negative value means the shipment ran at a loss.
 * calculatedAt is the server timestamp of when the calculation was performed, not the shipment
 * date. This DTO is never used as input — it is only returned in responses.
 */
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ProfitDTO extends BaseDTO {

  private final String shipmentRef;
  private final LocalDate shipmentDate;
  private final BigDecimal totalIncome;
  private final BigDecimal totalCosts;
  private final BigDecimal profitOrLoss;
  private final LocalDateTime calculatedAt;

  @Builder
  public ProfitDTO(
      Long id,
      String shipmentRef,
      LocalDate shipmentDate,
      BigDecimal totalIncome,
      BigDecimal totalCosts,
      BigDecimal profitOrLoss,
      LocalDateTime calculatedAt) {
    super(id);
    this.shipmentRef = shipmentRef;
    this.shipmentDate = shipmentDate;
    this.totalIncome = totalIncome;
    this.totalCosts = totalCosts;
    this.profitOrLoss = profitOrLoss;
    this.calculatedAt = calculatedAt;
  }
}
