package org.example.ash.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.ash.dto.request.AccountFilterRequest;
import org.example.ash.dto.request.CreateAccountRequest;
import org.example.ash.dto.response.AccountResponse;
import org.example.ash.dto.response.BaseResponse;
import org.example.ash.dto.response.PagedResponse;
import org.example.ash.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * GET /accounts
     *
     * Query params:
     *  accountNumber          – partial match (optional)
     *  availableBalanceFrom   – computed balance range lower bound (optional)
     *  availableBalanceTo     – computed balance range upper bound (optional)
     *  currency               – exact match, e.g. VND / USD (optional; used as currency-tab filter)
     *  status                 – exact match, e.g. ACTIVE / CLOSED (optional)
     *  cif                    – exact match (optional)
     *  primaryCif             – customer's primary CIF; accounts with this CIF are sorted first (optional)
     *  page                   – 0-based page index (default: 0)
     *  size                   – 10 (default) / 20 / 50
     *  viewMode               – LIST (default) / GRID
     *
     * Sort order (non-configurable, matches business rules):
     *  1. Primary-CIF accounts first
     *  2. displayOrder ASC NULLS LAST  (supports drag-and-drop reordering)
     *  3. createdAt ASC (oldest → newest within currency tab)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<PagedResponse<AccountResponse>>> getAccounts(
            @ModelAttribute AccountFilterRequest filter) {

        PagedResponse<AccountResponse> result = accountService.getAccounts(filter);
        return ResponseEntity.ok(BaseResponse.ok(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<AccountResponse>> getAccountById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(BaseResponse.ok(accountService.getAccountById(id)));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountResponse result = accountService.createAccount(request);
        return ResponseEntity.status(201).body(BaseResponse.created(result));
    }
}