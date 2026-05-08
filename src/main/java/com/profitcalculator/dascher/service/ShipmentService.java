package com.profitcalculator.dascher.service;

import com.profitcalculator.dascher.dto.ShipmentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Contract for full CRUD operations on shipments. Every write operation (create/update) also
 * computes and persists profitLoss on the shipment.
 */
public interface ShipmentService {

  // Returns all shipments paged. Pagination params come from the controller.
  Page<ShipmentDTO> findAll(Pageable pageable);

  // Fetches one shipment by DB id. Throws ResourceNotFoundException if not found.
  ShipmentDTO findById(Long id);

  // Creates a new shipment with its incomes and costs, then computes profitLoss.
  ShipmentDTO create(ShipmentDTO dto);

  // Replaces all fields, incomes, and costs on an existing shipment, then recomputes profitLoss.
  ShipmentDTO update(Long id, ShipmentDTO dto);

  // Deletes the shipment and cascades to all its incomes and costs.
  void delete(Long id);
}
