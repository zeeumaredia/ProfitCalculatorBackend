package com.profitcalculator.dascher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for a single cost entry within a shipment.
 *
 * <p>amount is the base cost; additionalCost is optional (defaults to 0 if omitted). costTypeName
 * must match an existing CostType.typeName (e.g. "FUEL", "CUSTOMS"). Both amount and additionalCost
 * are included in the profit formula: profitOrLoss = income − (amount + additionalCost).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class CostDTO extends BaseDTO {

  @NotNull(message = "Cost amount must not be null")
  @PositiveOrZero(message = "Cost amount must be zero or greater")
  private BigDecimal amount;

  @PositiveOrZero(message = "Additional cost must be zero or greater")
  private BigDecimal additionalCost;

  @NotBlank(message = "Cost type must not be blank")
  private String costTypeName; // maps to CostType.typeName

  @Builder
  public CostDTO(Long id, BigDecimal amount, BigDecimal additionalCost, String costTypeName) {
    super(id);
    this.amount = amount;
    this.additionalCost = additionalCost;
    this.costTypeName = costTypeName;
  }
}
