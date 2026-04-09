package org.example.ash.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.example.ash.entity.oracle.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String accountName;
    private String accountAliasName;
    private String status;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;

    /** Computed: currentBalance - blockedAmount + overdraftLimit + loanRemainingBalance */
    private BigDecimal calculatedAvailableBalance;

    private BigDecimal blockedAmount;
    private BigDecimal overdraftLimit;
    private BigDecimal loanRemainingBalance;
    private String currency;
    private String cif;
    private boolean primaryCif;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse from(Account account, String primaryCif) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountAliasName(account.getAccountAliasName())
                .status(account.getStatus())
                .currentBalance(account.getCurrentBalance())
                .availableBalance(account.getAvailableBalance())
                .calculatedAvailableBalance(account.calculateAvailableBalance())
                .blockedAmount(account.getBlockedAmount())
                .overdraftLimit(account.getOverdraftLimit())
                .loanRemainingBalance(account.getLoanRemainingBalance())
                .currency(account.getCurrency())
                .cif(account.getCif())
                .primaryCif(primaryCif != null && primaryCif.equals(account.getCif()))
                .displayOrder(account.getDisplayOrder())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}