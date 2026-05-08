package com.profitcalculator.dascher.util;

import com.profitcalculator.dascher.entity.Cost;
import com.profitcalculator.dascher.entity.Income;
import com.profitcalculator.dascher.entity.Shipment;
import java.math.BigDecimal;
import java.util.List;

/**
 * Stateless utility for profit/loss arithmetic.
 *
 * <p>Single source of truth for the formula: {@code profitOrLoss = totalIncome − SUM(cost.amount +
 * cost.additionalCost)}.
 *
 * <p>Kept separate from {@link com.profitcalculator.dascher.mapper.ProfitMapper} or as a Spring
 * Component + that each class has one reason to change: this class changes when the business
 * formula changes; the mapper changes when the DTO structure changes.
 */
public final class ProfitCalculator {

  private ProfitCalculator() {}

  /** Sums all income amounts, treating {@code null} amounts as zero. */
  public static BigDecimal sumIncome(List<Income> incomes) {
    if (incomes == null) return BigDecimal.ZERO;
    return incomes.stream()
        .map(i -> i.getAmount() == null ? BigDecimal.ZERO : i.getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Sums all cost amounts (base + additional), treating {@code null} amounts as zero. */
  public static BigDecimal sumCosts(List<Cost> costs) {
    if (costs == null) return BigDecimal.ZERO;
    return costs.stream()
        .map(
            c -> {
              BigDecimal amount = c.getAmount() == null ? BigDecimal.ZERO : c.getAmount();
              BigDecimal additional =
                  c.getAdditionalCost() == null ? BigDecimal.ZERO : c.getAdditionalCost();
              return amount.add(additional);
            })
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Computes profit/loss from a {@link Shipment}'s loaded collections.
   *
   * <p>Callers must ensure {@code shipment.getIncomes()} and {@code shipment.getCosts()} are
   * initialised (eagerly fetched or lazily loaded within an active transaction).
   */
  public static BigDecimal computeProfitLoss(Shipment shipment) {
    return sumIncome(shipment.getIncomes()).subtract(sumCosts(shipment.getCosts()));
  }
}
