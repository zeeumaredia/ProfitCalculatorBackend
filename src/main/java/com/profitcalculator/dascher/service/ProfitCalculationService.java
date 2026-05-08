package com.profitcalculator.dascher.service;

import com.profitcalculator.dascher.dto.ProfitDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for profit/loss calculation operations. Separate from ShipmentService to keep
 * calculation concerns isolated from CRUD.
 */
public interface ProfitCalculationService {

  // Computes and persists profitLoss for a single shipment, returns the full breakdown.
  ProfitDTO calculate(Long shipmentId);

  // Returns profit/loss summary for all shipments, paged.
  Page<ProfitDTO> findAll(Pageable pageable);
}
