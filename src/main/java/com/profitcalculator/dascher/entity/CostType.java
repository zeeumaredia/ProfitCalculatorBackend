package com.profitcalculator.dascher.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Reference/lookup table for cost categories (e.g. FUEL, CUSTOMS, LABOR, TOLL, STORAGE). These are
 * seeded at startup and are not expected to change at runtime. Cost entries reference this table by
 * typeName when a shipment is created or updated.
 */
@Entity
@Table(name = "cost_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cost_type_id")
  private Long costTypeId;

  @Column(name = "type_name", nullable = false, unique = true, length = 100)
  private String typeName;

  @Column(name = "description", length = 255)
  private String description;
}
