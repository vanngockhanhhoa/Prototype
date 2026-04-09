package org.example.ash.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {

    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    @Size(max = 255, message = "Account name must not exceed 255 characters")
    private String accountName;

    @Size(max = 255, message = "Account alias name must not exceed 255 characters")
    private String accountAliasName;

    @NotBlank(message = "Currency is required")
    @Size(max = 10, message = "Currency must not exceed 10 characters")
    private String currency;

    @NotBlank(message = "CIF is required")
    @Size(max = 50, message = "CIF must not exceed 50 characters")
    private String cif;

    @NotNull(message = "Current balance is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Current balance must be >= 0")
    private BigDecimal currentBalance;

    @DecimalMin(value = "0.0", inclusive = true, message = "Blocked amount must be >= 0")
    private BigDecimal blockedAmount;

    @DecimalMin(value = "0.0", inclusive = true, message = "Overdraft limit must be >= 0")
    private BigDecimal overdraftLimit;

    @DecimalMin(value = "0.0", inclusive = true, message = "Loan remaining balance must be >= 0")
    private BigDecimal loanRemainingBalance;
}