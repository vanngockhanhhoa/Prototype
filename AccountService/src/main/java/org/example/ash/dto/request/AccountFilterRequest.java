package org.example.ash.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountFilterRequest {

    // ── Column filters ─────────────────────────────────────────────────────────

    /** Partial match on account number */
    private String accountNumber;

    /** Calculated available balance range (currentBalance - blockedAmount + overdraftLimit + loanRemainingBalance) */
    private BigDecimal availableBalanceFrom;
    private BigDecimal availableBalanceTo;

    /** Currency tab filter (VND, USD, …) */
    private String currency;

    /** Status filter (ACTIVE / CLOSED / …) */
    private String status;

    /** Filter by specific CIF */
    private String cif;

    // ── Primary-CIF ordering ───────────────────────────────────────────────────

    /**
     * The customer's primary CIF.
     * Accounts whose CIF matches this value are displayed above all others.
     */
    private String primaryCif;

    // ── Pagination ─────────────────────────────────────────────────────────────

    /** 0-based page index */
    private int page = 0;

    /** Rows per page: 10 (default) / 20 / 50 */
    private int size = 10;

    // ── View mode ──────────────────────────────────────────────────────────────

    /** LIST (default) or GRID */
    private ViewMode viewMode = ViewMode.LIST;

    public enum ViewMode {
        LIST, GRID
    }
}