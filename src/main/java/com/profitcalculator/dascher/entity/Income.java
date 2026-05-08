package com.profitcalculator.dascher.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Represents a single revenue entry for a shipment (e.g. freight charge, handling fee). Multiple
 * income records can belong to one shipment — their amounts are summed during profit calculation.
 */
@Entity
@Table(name = "income")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "income_id")
  private Long incomeId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shipment_id", nullable = false)
  private Shipment shipment;

  @Column(name = "amount", nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
