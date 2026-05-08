package com.profitcalculator.dascher.repository;

import com.profitcalculator.dascher.entity.Shipment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

  /** Eagerly fetches incomes and costs — avoids N+1 on single-record lookups. */
  @Override
  @EntityGraph(attributePaths = {"incomes", "costs"})
  Optional<Shipment> findById(Long id);

  // No @EntityGraph — applying it to a Pageable query forces in-memory pagination (HHH90003004);
  // SUBSELECT on the entity handles collections instead.
  @Override
  Page<Shipment> findAll(Pageable pageable);
}
