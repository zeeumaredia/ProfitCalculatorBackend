package com.profitcalculator.dascher.service.impl;

import com.profitcalculator.dascher.dto.ProfitDTO;
import com.profitcalculator.dascher.entity.Shipment;
import com.profitcalculator.dascher.exception.ResourceNotFoundException;
import com.profitcalculator.dascher.mapper.ProfitMapper;
import com.profitcalculator.dascher.repository.ShipmentRepository;
import com.profitcalculator.dascher.service.ProfitCalculationService;
import com.profitcalculator.dascher.util.ProfitCalculator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Computes profit/loss for shipments and persists the result back to the shipment record.
 *
 * <p>Business rule: profitOrLoss = totalIncome − totalCosts (base + additional). A negative result
 * means the shipment ran at a loss.
 */
@Service
@RequiredArgsConstructor
public class ProfitCalculationServiceImpl implements ProfitCalculationService {

  private final ShipmentRepository shipmentRepository;

  private static final String SHIPMENT_NOT_FOUND = "Shipment not found with id: ";

  /**
   * Computes profit/loss for the given shipment, saves it on the entity, and returns the DTO.
   *
   * <p>Income and cost collections are already eagerly loaded by the {@code @EntityGraph} on {@link
   * ShipmentRepository#findById} — no second/extra repository calls needed here.
   */
  // Not readOnly — this method persists the computed profitLoss back to the shipment; a read-only
  // transaction silently skips the save on some JDBC drivers.
  @Override
  @Transactional
  public ProfitDTO calculate(Long shipmentId) {
    Shipment shipment =
        shipmentRepository
            .findById(shipmentId)
            .orElseThrow(() -> new ResourceNotFoundException(SHIPMENT_NOT_FOUND + shipmentId));

    ProfitDTO dto = buildProfitDto(shipment);
    shipment.setProfitLoss(dto.getProfitOrLoss());
    shipmentRepository.save(shipment);
    return dto;
  }

  /**
   * Returns paginated profit/loss for all shipments.
   *
   * <p>Collections are loaded lazily via Hibernate's {@code SUBSELECT} fetch strategy defined on
   * the {@link Shipment} entity — one query per collection for the whole page, not per row.
   * (Avoiding N+1 Issue)
   */
  @Override
  @Transactional(readOnly = true)
  public Page<ProfitDTO> findAll(Pageable pageable) {
    return shipmentRepository.findAll(pageable).map(this::buildProfitDto);
  }

  // --- private helpers ---

  private ProfitDTO buildProfitDto(Shipment shipment) {
    BigDecimal totalIncome = ProfitCalculator.sumIncome(shipment.getIncomes());
    BigDecimal totalCosts = ProfitCalculator.sumCosts(shipment.getCosts());
    return ProfitMapper.toDto(shipment, totalIncome, totalCosts, LocalDateTime.now());
  }
}
