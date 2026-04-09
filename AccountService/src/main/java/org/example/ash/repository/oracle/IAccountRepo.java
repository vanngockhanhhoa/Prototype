package org.example.ash.repository.oracle;

import org.example.ash.entity.oracle.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IAccountRepo extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {

    boolean existsByAccountNumber(String accountNumber);
}
