/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.TransactionEntity;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDto;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CategoryRepository;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

import static org.panteleyev.money.backend.util.JsonUtil.objectMapper;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final CurrencyRepository currencyRepository;
    private final IconRepository iconRepository;
    private final TransactionRepository transactionRepository;
    private final EntityToDtoConverter converter;

    public AccountService(
            AccountRepository accountRepository,
            CategoryRepository categoryRepository,
            CurrencyRepository currencyRepository,
            IconRepository iconRepository,
            TransactionRepository transactionRepository,
            EntityToDtoConverter converter
    )
    {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.currencyRepository = currencyRepository;
        this.iconRepository = iconRepository;
        this.transactionRepository = transactionRepository;
        this.converter = converter;
    }

    public List<AccountFlatDto> getAll() {
        return accountRepository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = accountRepository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<AccountFlatDto> get(UUID uuid) {
        return accountRepository.findById(uuid).map(converter::entityToFlatDto);
    }

    public AccountFlatDto put(AccountFlatDto account) {
        var category = categoryRepository.findById(account.getCategoryUuid()).orElseThrow();
        var currency = account.getCurrencyUuid() == null ?
                null : currencyRepository.getReferenceById(account.getCurrencyUuid());
        var icon = account.getIconUuid() == null ?
                null : iconRepository.getReferenceById(account.getIconUuid());

        return converter.entityToFlatDto(accountRepository.save(converter.dtoToEntity(
                account, category, currency, icon
        )));
    }

    @Transactional
    public Collection<AccountFlatDto> updateBalances(Collection<UUID> accountIds) {
        var toUpdate = new ArrayList<AccountEntity>(accountIds.size());

        accountRepository.findAllById(accountIds).forEach(entity -> {
            var total = calculateBalance(entity, false, _ -> true);
            var waiting = calculateBalance(entity, false, t -> !t.isChecked());
            var updated = entity.updateBalance(total, waiting);
            toUpdate.add(updated);
        });

        var updated = accountRepository.saveAll(toUpdate);
        return updated.stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateBalance(AccountEntity account, boolean total, Predicate<TransactionEntity> filter) {
        var initialBalance = total ?
                account.getOpeningBalance().add(account.getAccountLimit()) :
                BigDecimal.ZERO;

        return transactionRepository.streamByAccountId(account.getUuid())
                .filter(filter)
                .filter(t -> t.getParent() == null)
                .map(t -> Objects.equals(account.getUuid(), t.getAccountCredited().getUuid()) ?
                        t.getCreditAmount() :
                        t.getAmount().negate())
                .reduce(initialBalance, BigDecimal::add);
    }

    public long getCount(boolean inactive) {
        return inactive ? accountRepository.count() : accountRepository.getEnabledCount();
    }
}
