/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.controller;

import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.AccountInput;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class AccountGraphQLController {
    private final IconService iconService;
    private final CategoryService categoryService;
    private final CurrencyService currencyService;
    private final AccountService accountService;

    public AccountGraphQLController(
            IconService iconService,
            CategoryService categoryService,
            CurrencyService currencyService,
            AccountService accountService
    ) {
        this.iconService = iconService;
        this.categoryService = categoryService;
        this.currencyService = currencyService;
        this.accountService = accountService;
    }

    @QueryMapping
    public List<Account> accounts() {
        return accountService.getAll();
    }

    @QueryMapping
    public Account account(@Argument UUID uuid) {
        return accountService.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Account", uuid));
    }

    @MutationMapping
    public Account createAccount(@Argument AccountInput input) {
        var category = categoryService.get(input.categoryUuid()).orElseThrow();
        var account = new Account.Builder()
                .name(input.name())
                .comment(input.comment())
                .accountNumber(input.accountNumber())
                .openingBalance(input.openingBalance())
                .accountLimit(input.accountLimit())
                .currencyRate(input.currencyRate())
                .type(category.type())
                .categoryUuid(input.categoryUuid())
                .currencyUuid(input.currencyUuid())
                .enabled(input.enabled())
                .interest(input.interest())
                .closingDate(input.closingDate())
                .iconUuid(input.iconUuid())
                .cardType(input.cardType())
                .cardNumber(input.cardNumber())
                .total(input.total())
                .totalWaiting(input.totalWaiting())
                .build();
        return accountService.put(account)
                .orElseThrow(() -> new GraphQLCreateException("Account"));
    }

    @MutationMapping
    public Account updateAccount(
            @Argument UUID uuid,
            @Argument AccountInput input
    ) {
        var builder = accountService.get(uuid)
                .map(Account.Builder::new)
                .orElseThrow();

        var category = categoryService.get(input.categoryUuid()).orElseThrow();
        var account = builder.name(input.name())
                .comment(input.comment())
                .accountNumber(input.accountNumber())
                .openingBalance(input.openingBalance())
                .accountLimit(input.accountLimit())
                .currencyRate(input.currencyRate())
                .type(category.type())
                .categoryUuid(input.categoryUuid())
                .currencyUuid(input.currencyUuid())
                .enabled(input.enabled())
                .interest(input.interest())
                .closingDate(input.closingDate())
                .iconUuid(input.iconUuid())
                .cardType(input.cardType())
                .cardNumber(input.cardNumber())
                .total(input.total())
                .totalWaiting(input.totalWaiting())
                .modified(System.currentTimeMillis())
                .build();
        return accountService.put(account)
                .orElseThrow(() -> new GraphQLUpdateException("Account", uuid));
    }

    @SchemaMapping
    public Category category(Account account) {
        return categoryService.get(account.categoryUuid()).orElseThrow();
    }

    @SchemaMapping
    public Icon icon(Account account) {
        return account.iconUuid() == null ?
                null : iconService.get(account.iconUuid()).orElseThrow();
    }

    @SchemaMapping
    public Currency currency(Account account) {
        return account.currencyUuid() == null ?
                null : currencyService.get(account.currencyUuid()).orElseThrow();
    }
}
