package com.profitcalculator.dascher.service.impl;

import com.profitcalculator.dascher.dto.CostDTO;
import com.profitcalculator.dascher.dto.IncomeDTO;
import com.profitcalculator.dascher.dto.ShipmentDTO;
import com.profitcalculator.dascher.entity.Cost;
import com.profitcalculator.dascher.entity.CostType;
import com.profitcalculator.dascher.entity.Income;
import com.profitcalculator.dascher.entity.Shipment;
import com.profitcalculator.dascher.exception.ResourceNotFoundException;
import com.profitcalculator.dascher.mapper.ShipmentMapper;
import com.profitcalculator.dascher.repository.CostTypeRepository;
import com.profitcalculator.dascher.repository.ShipmentRepository;
import com.profitcalculator.dascher.service.ShipmentService;
import com.profitcalculator.dascher.util.ProfitCalculator;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles shipment CRUD and keeps profitLoss in sync after every write.
 *
 * <p>Business rule: profitLoss must be recomputed and saved whenever incomes or costs change. On
 * update, all existing incomes and costs are cleared and replaced — no partial patching.
 */
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

  private final ShipmentRepository shipmentRepository;
  private final CostTypeRepository costTypeRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<ShipmentDTO> findAll(Pageable pageable) {
    return shipmentRepository.findAll(pageable).map(ShipmentMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public ShipmentDTO findById(Long id) {
    return ShipmentMapper.toDto(findShipmentOrThrow(id));
  }

  // Takes ShipmentDTO with incomes and costs, builds the entity graph, computes profitLoss,
  // persists all.
  @Override
  @Transactional
  public ShipmentDTO create(ShipmentDTO dto) {
    Shipment shipment = ShipmentMapper.toEntity(dto);
    addIncomes(shipment, dto.getIncomes());
    addCosts(shipment, dto.getCosts());
    shipment.setProfitLoss(ProfitCalculator.computeProfitLoss(shipment));
    return ShipmentMapper.toDto(shipmentRepository.save(shipment));
  }

  // Full replace: clears all existing incomes and costs before adding the new ones from dto.
  // orphanRemoval=true on the entity ensures cleared children are deleted from the DB.
  @Override
  @Transactional
  public ShipmentDTO update(Long id, ShipmentDTO dto) {
    Shipment shipment = findShipmentOrThrow(id);

    shipment.setShipmentRef(dto.getShipmentRef());
    shipment.setDescription(dto.getDescription());
    shipment.setShipmentDate(dto.getShipmentDate());

    shipment.getIncomes().clear();
    shipment.getCosts().clear();

    addIncomes(shipment, dto.getIncomes());
    addCosts(shipment, dto.getCosts());
    shipment.setProfitLoss(ProfitCalculator.computeProfitLoss(shipment));

    return ShipmentMapper.toDto(shipmentRepository.save(shipment));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    shipmentRepository.delete(findShipmentOrThrow(id));
  }

  // --- private helpers ---

  // Loads the shipment or throws 404 — used by all methods that need an existing record.
  private Shipment findShipmentOrThrow(Long id) {
    return shipmentRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Shipment not found with id: " + id));
  }

  // Converts IncomeDTO list to Income entities and links them to the shipment.
  private void addIncomes(Shipment shipment, List<IncomeDTO> incomeDtos) {
    if (incomeDtos == null || incomeDtos.isEmpty()) return;
    incomeDtos.forEach(
        dto -> {
          Income income = Income.builder().amount(dto.getAmount()).shipment(shipment).build();
          shipment.getIncomes().add(income);
        });
  }

  // Converts CostDTO list to Cost entities, resolving costTypeName against the reference table.
  // Throws 404 if any costTypeName doesn't match a known CostType.
  private void addCosts(Shipment shipment, List<CostDTO> costDtos) {
    if (costDtos == null || costDtos.isEmpty()) return;
    costDtos.forEach(
        dto -> {
          CostType costType =
              costTypeRepository
                  .findByTypeName(dto.getCostTypeName())
                  .orElseThrow(
                      () ->
                          new ResourceNotFoundException(
                              "Cost type not found: " + dto.getCostTypeName()));

          Cost cost =
              Cost.builder()
                  .amount(dto.getAmount())
                  .additionalCost(
                      dto.getAdditionalCost() != null ? dto.getAdditionalCost() : BigDecimal.ZERO)
                  .costType(costType)
                  .shipment(shipment)
                  .build();

          shipment.getCosts().add(cost);
        });
  }
}
