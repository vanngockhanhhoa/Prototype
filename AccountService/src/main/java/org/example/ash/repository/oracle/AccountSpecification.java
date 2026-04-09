package org.example.ash.repository.oracle;

import jakarta.persistence.criteria.*;
import org.example.ash.dto.request.AccountFilterRequest;
import org.example.ash.entity.oracle.Account;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class AccountSpecification {

    private AccountSpecification() {}

    /**
     * Builds a Specification from the given filter.
     * Filters applied:
     *  - accountNumber   : case-insensitive contains
     *  - currency        : exact match
     *  - status          : exact match
     *  - cif             : exact match
     *  - availableBalance range : computed as (currentBalance - blockedAmount + overdraftLimit + loanRemainingBalance)
     */
    public static Specification<Account> of(AccountFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getAccountNumber() != null && !filter.getAccountNumber().isBlank()) {
                predicates.add(cb.like(
                        cb.lower(root.get("accountNumber")),
                        "%" + filter.getAccountNumber().toLowerCase() + "%"
                ));
            }

            if (filter.getCurrency() != null && !filter.getCurrency().isBlank()) {
                predicates.add(cb.equal(root.get("currency"), filter.getCurrency().toUpperCase()));
            }

            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("status")), filter.getStatus().toUpperCase()));
            }

            if (filter.getCif() != null && !filter.getCif().isBlank()) {
                predicates.add(cb.equal(root.get("cif"), filter.getCif()));
            }

            // Computed available balance filter
            if (filter.getAvailableBalanceFrom() != null || filter.getAvailableBalanceTo() != null) {
                Expression<BigDecimal> calcBalance = computedAvailableBalance(root, cb);
                if (filter.getAvailableBalanceFrom() != null && filter.getAvailableBalanceTo() != null) {
                    predicates.add(cb.between(calcBalance,
                            filter.getAvailableBalanceFrom(), filter.getAvailableBalanceTo()));
                } else if (filter.getAvailableBalanceFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(calcBalance, filter.getAvailableBalanceFrom()));
                } else {
                    predicates.add(cb.lessThanOrEqualTo(calcBalance, filter.getAvailableBalanceTo()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Builds a sort order list for account list:
     *  1. Primary-CIF accounts first (when primaryCif is provided)
     *  2. displayOrder ASC NULLS LAST
     *  3. createdAt ASC (oldest → newest, default within currency tab)
     */
    public static List<Order> buildOrder(Root<Account> root, CriteriaBuilder cb, String primaryCif) {
        List<Order> orders = new ArrayList<>();

        if (primaryCif != null && !primaryCif.isBlank()) {
            // CASE WHEN cif = :primaryCif THEN 0 ELSE 1 END ASC  → primary CIF rows first
            Expression<Integer> cifPriority = cb.<Integer>selectCase()
                    .when(cb.equal(root.get("cif"), primaryCif), 0)
                    .otherwise(1);
            orders.add(cb.asc(cifPriority));
        }

        // displayOrder ASC NULLS LAST (accounts with explicit order before unordered ones)
        orders.add(cb.asc(cb.coalesce(root.<Integer>get("displayOrder"), Integer.MAX_VALUE)));

        // Oldest to newest within same display order
        orders.add(cb.asc(root.get("createdAt")));

        return orders;
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    /**
     * SQL expression: currentBalance - COALESCE(blockedAmount,0)
     *                               + COALESCE(overdraftLimit,0)
     *                               + COALESCE(loanRemainingBalance,0)
     */
    static Expression<BigDecimal> computedAvailableBalance(Root<Account> root, CriteriaBuilder cb) {
        Expression<BigDecimal> current  = root.get("currentBalance");
        Expression<BigDecimal> blocked  = cb.coalesce(root.<BigDecimal>get("blockedAmount"), BigDecimal.ZERO);
        Expression<BigDecimal> overdraft = cb.coalesce(root.<BigDecimal>get("overdraftLimit"), BigDecimal.ZERO);
        Expression<BigDecimal> loan     = cb.coalesce(root.<BigDecimal>get("loanRemainingBalance"), BigDecimal.ZERO);

        return cb.sum(cb.sum(cb.diff(current, blocked), overdraft), loan);
    }
}