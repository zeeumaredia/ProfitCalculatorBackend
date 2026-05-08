package com.profitcalculator.dascher.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for a single income entry within a shipment. amount must be a positive value — zero or
 * negative income makes no business sense here.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class IncomeDTO extends BaseDTO {

  @NotNull(message = "Income amount must not be null")
  @Positive(message = "Income amount must be greater than zero")
  private BigDecimal amount;

  @Builder
  public IncomeDTO(Long id, BigDecimal amount) {
    super(id);
    this.amount = amount;
  }
}
