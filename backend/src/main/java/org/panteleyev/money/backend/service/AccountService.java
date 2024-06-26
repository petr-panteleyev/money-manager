/*
 Copyright © 2022-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final Cache cache;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            Cache accountCache
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cache = accountCache;
    }

    public List<Account> getAll() {
        return accountRepository.getAll();
    }

    public Optional<Account> get(UUID uuid) {
        return ServiceUtil.get(accountRepository, cache, uuid);
    }

    public Optional<Account> put(Account account) {
        return ServiceUtil.put(accountRepository, cache, account);
    }

    public Collection<Account> updateBalances(Collection<UUID> accountIds) {
        var result = new ArrayList<Account>(accountIds.size());
        for (UUID uuid : accountIds) {
            accountRepository.get(uuid).ifPresent(account -> {
                var total = calculateBalance(account, false, _ -> true);
                var waiting = calculateBalance(account, false, t -> !t.checked());
                put(account.updateBalance(total, waiting)).ifPresent(result::add);
            });
        }

        return result;
    }

    public BigDecimal calculateBalance(Account account, boolean total, Predicate<Transaction> filter) {
        var initialBalance = total ?
                account.openingBalance().add(account.accountLimit()) :
                BigDecimal.ZERO;

        return transactionRepository.getByAccountId(account.uuid()).stream()
                .filter(filter)
                .filter(t -> t.parentUuid() == null)
                .map(t -> Objects.equals(account.uuid(), t.accountCreditedUuid()) ?
                        t.creditAmount() :
                        Transaction.getNegatedAmount(t))
                .reduce(initialBalance, BigDecimal::add);
    }

    public int getCount(boolean inactive) {
        return accountRepository.getCount(inactive);
    }
}
