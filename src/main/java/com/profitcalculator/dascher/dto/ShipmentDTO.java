package com.profitcalculator.dascher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request/response DTO for shipment operations.
 *
 * <p>On create and update: shipmentRef and shipmentDate are required. incomes and costs are
 * optional — a shipment can exist with no entries. profitLoss is computed server-side and returned
 * in the response; it should not be sent by the client. Null fields are excluded from JSON output
 * to keep responses clean.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ShipmentDTO extends BaseDTO {

  @NotBlank(message = "Shipment reference is required")
  private String shipmentRef;

  private String description;

  @NotNull(message = "Shipment date is required")
  private LocalDate shipmentDate;

  @Valid private List<IncomeDTO> incomes;

  @Valid private List<CostDTO> costs;

  private BigDecimal profitLoss;

  @Builder
  public ShipmentDTO(
      Long id,
      String shipmentRef,
      String description,
      LocalDate shipmentDate,
      List<IncomeDTO> incomes,
      List<CostDTO> costs,
      BigDecimal profitLoss) {
    super(id);
    this.shipmentRef = shipmentRef;
    this.description = description;
    this.shipmentDate = shipmentDate;
    this.incomes = incomes;
    this.costs = costs;
    this.profitLoss = profitLoss;
  }
}
