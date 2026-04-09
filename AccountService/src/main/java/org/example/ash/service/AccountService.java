package org.example.ash.service;

import org.example.ash.dto.request.AccountFilterRequest;
import org.example.ash.dto.request.CreateAccountRequest;
import org.example.ash.dto.response.AccountResponse;
import org.example.ash.dto.response.PagedResponse;

public interface AccountService {

    PagedResponse<AccountResponse> getAccounts(AccountFilterRequest filter);

    AccountResponse createAccount(CreateAccountRequest request);

    AccountResponse getAccountById(Long id);
}