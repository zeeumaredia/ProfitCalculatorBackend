package com.profitcalculator.dascher.mapper;

import com.profitcalculator.dascher.dto.CostDTO;
import com.profitcalculator.dascher.dto.IncomeDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import com.profitcalculator.dascher.entity.Cost;
import com.profitcalculator.dascher.entity.Income;
import com.profitcalculator.dascher.entity.Shipment;
import java.util.Collections;
import java.util.List;

/**
 * Converts between Shipment entity and ShipmentDTO. No business logic here — just field mapping.
 * Any calculation belongs in ProfitCalculator.
 */
public class ShipmentMapper {

  private ShipmentMapper() {}

  // Maps a full Shipment entity (with its incomes and costs) to a ShipmentDTO for the response.
  public static ShipmentDTO toDto(Shipment shipment) {
    if (shipment == null) return null;

    List<IncomeDTO> incomeDTOs =
        shipment.getIncomes() == null
            ? Collections.emptyList()
            : shipment.getIncomes().stream().map(ShipmentMapper::toIncomeDto).toList();

    List<CostDTO> costDTOs =
        shipment.getCosts() == null
            ? Collections.emptyList()
            : shipment.getCosts().stream().map(ShipmentMapper::toCostDto).toList();

    return ShipmentDTO.builder()
        .id(shipment.getShipmentId())
        .shipmentRef(shipment.getShipmentRef())
        .description(shipment.getDescription())
        .shipmentDate(shipment.getShipmentDate())
        .incomes(incomeDTOs)
        .costs(costDTOs)
        .profitLoss(shipment.getProfitLoss())
        .build();
  }

  // Maps a single Income entity to IncomeDTO (id + amount only).
  public static IncomeDTO toIncomeDto(Income income) {
    if (income == null) return null;
    return IncomeDTO.builder().id(income.getIncomeId()).amount(income.getAmount()).build();
  }

  // Maps a Cost entity to CostDTO, resolving the CostType name for the response.
  public static CostDTO toCostDto(Cost cost) {
    if (cost == null) return null;
    return CostDTO.builder()
        .id(cost.getCostId())
        .amount(cost.getAmount())
        .additionalCost(cost.getAdditionalCost())
        .costTypeName(cost.getCostType().getTypeName())
        .build();
  }

  // Builds a new Shipment entity from the DTO — incomes and costs are added separately in the
  // service.
  public static Shipment toEntity(ShipmentDTO shipmentDTO) {
    if (shipmentDTO == null) return null;
    return Shipment.builder()
        .shipmentRef(shipmentDTO.getShipmentRef())
        .description(shipmentDTO.getDescription())
        .shipmentDate(shipmentDTO.getShipmentDate())
        .build();
  }
}
