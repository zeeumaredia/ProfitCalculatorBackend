package com.profitcalculator.dascher.dto;

import lombok.Getter;

/**
 * Sealed base for all DTOs — holds the database ID, which is populated on responses but not
 * required on create requests. Sealed so no unexpected DTO subclass can be introduced outside this
 * package.
 */
@Getter
public abstract sealed class BaseDTO permits CostDTO, IncomeDTO, ShipmentDTO, ProfitDTO {

  private Long id;

  protected BaseDTO() {}

  protected BaseDTO(Long id) {
    this.id = id;
  }
}
