package com.profitcalculator.dascher.repository;

import com.profitcalculator.dascher.entity.CostType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for CostType reference data. Primarily used to validate and resolve cost type names
 * during shipment creation/update.
 */
@Repository
public interface CostTypeRepository extends JpaRepository<CostType, Long> {

  // Looks up a cost type by its name (e.g. "FUEL"). Used when linking a cost entry to its type.
  Optional<CostType> findByTypeName(String typeName);
}
