package com.profitcalculator.dascher.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents a single cost entry for a shipment (e.g. FUEL, CUSTOMS, LABOR).
 *
 * <p>Each cost has a base amount and an optional additionalCost (surcharges, overrides). Both are
 * summed together when computing total costs for the shipment. The cost type must already exist in
 * the cost_type reference table.
 */
@Entity
@Table(name = "cost")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cost {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cost_id")
  private Long costId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  private Shipment shipment;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cost_type_id", nullable = false)
  private CostType costType;

  // BigDecimal instead of double — IEEE 754 binary arithmetic accumulates rounding errors that
  // compound across financial calculations.
  @Column(name = "amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Builder.Default
  @Column(name = "additional_cost", nullable = false, precision = 18, scale = 2)
  private BigDecimal additionalCost = BigDecimal.ZERO;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
