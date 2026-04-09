package org.example.ash.service.impl;

import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.example.ash.dto.request.AccountFilterRequest;
import org.example.ash.dto.request.CreateAccountRequest;
import org.example.ash.dto.response.AccountResponse;
import org.example.ash.dto.response.PagedResponse;
import org.example.ash.entity.oracle.Account;
import org.example.ash.repository.oracle.AccountSpecification;
import org.example.ash.repository.oracle.IAccountRepo;
import org.example.ash.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final Set<Integer> ALLOWED_PAGE_SIZES = Set.of(10, 20, 50);

    private final IAccountRepo accountRepo;

    @Override
    public PagedResponse<AccountResponse> getAccounts(AccountFilterRequest filter) {
        int size = ALLOWED_PAGE_SIZES.contains(filter.getSize()) ? filter.getSize() : 10;

        Specification<Account> spec = AccountSpecification.of(filter);

        // Attach dynamic ORDER BY via Specification so we can use CriteriaBuilder
        Specification<Account> specWithOrder = (root, query, cb) -> {
            Specification<Account> base = AccountSpecification.of(filter);
            if (query != null && query.getResultType() != Long.class) {
                // Only apply ordering on the data query, not the count query
                List<Order> orders = AccountSpecification.buildOrder(
                        (Root<Account>) root, cb, filter.getPrimaryCif());
                query.orderBy(orders);
            }
            return base.toPredicate(root, query, cb);
        };

        Page<Account> page = accountRepo.findAll(specWithOrder, PageRequest.of(filter.getPage(), size));

        List<AccountResponse> content = page.getContent().stream()
                .map(a -> AccountResponse.from(a, filter.getPrimaryCif()))
                .toList();

        return PagedResponse.<AccountResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account not found: " + id));
        return AccountResponse.from(account, null);
    }

    @Override
    public AccountResponse createAccount(CreateAccountRequest request) {
        if (accountRepo.existsByAccountNumber(request.getAccountNumber())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Account number already exists: " + request.getAccountNumber());
        }

        Account account = new Account();
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountName(request.getAccountName());
        account.setAccountAliasName(request.getAccountAliasName());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setCif(request.getCif());
        account.setStatus("ACTIVE");
        account.setCurrentBalance(request.getCurrentBalance());
        account.setBlockedAmount(request.getBlockedAmount());
        account.setOverdraftLimit(request.getOverdraftLimit());
        account.setLoanRemainingBalance(request.getLoanRemainingBalance());

        // availableBalance mirrors the computed value at creation time
        account.setAvailableBalance(account.calculateAvailableBalance());

        Account saved = accountRepo.save(account);
        return AccountResponse.from(saved, null);
    }
}