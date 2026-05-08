package com.profitcalculator.dascher.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Root aggregate for a shipment record.
 *
 * <p>A shipment owns its incomes and costs — deleting a shipment cascades to both. The profitLoss
 * field is computed and stored whenever the shipment is saved or recalculated. SUBSELECT fetch on
 * collections avoids N+1 when loading a page of shipments.
 */
@Entity
@Table(name = "shipment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Shipment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "shipment_id")
  private Long shipmentId;

  // Business identifier — must be unique across all shipments (e.g. "SHP-2024-001")
  @Column(name = "shipment_ref", nullable = false, unique = true, length = 100)
  private String shipmentRef;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "shipment_date")
  private LocalDate shipmentDate;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  // Persisted result of: totalIncome − totalCosts. Negative means a loss.
  @Column(name = "profitloss")
  private BigDecimal profitLoss;

  // @Builder.Default prevents a null list — Hibernate cannot iterate a null collection during flush
  // and would throw NPE on cascade-persist.
  @Builder.Default
  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  private List<Income> incomes = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
  @Fetch(FetchMode.SUBSELECT)
  private List<Cost> costs = new ArrayList<>();
}
