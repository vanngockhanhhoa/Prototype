package org.example.ash.entity.oracle;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.ash.entity.Auditable;

import java.math.BigDecimal;

@Entity
@Table(name = "ACCOUNT")
@Getter
@Setter
public class Account extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // 1. Account Number
    @Column(name = "ACCOUNT_NUMBER", nullable = false, unique = true, length = 50)
    private String accountNumber;

    // 2. Account Name
    @Column(name = "ACCOUNT_NAME", nullable = false, length = 255)
    private String accountName;

    // 3. Account Alias Name (optional)
    @Column(name = "ACCOUNT_ALIAS_NAME", length = 255)
    private String accountAliasName;

    // 4. Status (Active / Closed)
    @Column(name = "STATUS", nullable = false, length = 20)
    private String status;

    // 5. Current Balance
    @Column(name = "CURRENT_BALANCE", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentBalance;

    // 6. Available Balance
    @Column(name = "AVAILABLE_BALANCE", nullable = false, precision = 19, scale = 4)
    private BigDecimal availableBalance;

    // 7. Currency
    @Column(name = "CURRENCY", nullable = false, length = 10)
    private String currency;

    // Extra (theo business bạn mô tả)

    // CIF (Customer ID)
    @Column(name = "CIF", nullable = false, length = 50)
    private String cif;

    // Blocked amount
    @Column(name = "BLOCKED_AMOUNT", precision = 19, scale = 4)
    private BigDecimal blockedAmount;

    // Overdraft limit
    @Column(name = "OVERDRAFT_LIMIT", precision = 19, scale = 4)
    private BigDecimal overdraftLimit;

    // Remaining loan balance
    @Column(name = "LOAN_REMAINING_BALANCE", precision = 19, scale = 4)
    private BigDecimal loanRemainingBalance;

    // Display order for drag-and-drop (nullable = sorted last by default)
    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    // ===== Business method =====
    public BigDecimal calculateAvailableBalance() {
        BigDecimal result = currentBalance;

        if (blockedAmount != null) {
            result = result.subtract(blockedAmount);
        }
        if (overdraftLimit != null) {
            result = result.add(overdraftLimit);
        }
        if (loanRemainingBalance != null) {
            result = result.add(loanRemainingBalance);
        }

        return result;
    }
}