/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.AccountInput;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.model.Account;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountMutation implements GraphQLMutationResolver {
    private final CategoryService categoryService;
    private final AccountService accountService;

    public AccountMutation(CategoryService categoryService, AccountService accountService) {
        this.categoryService = categoryService;
        this.accountService = accountService;
    }

    public Account createAccount(AccountInput input) {
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

    public Account updateAccount(UUID uuid, AccountInput input) {
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
}
