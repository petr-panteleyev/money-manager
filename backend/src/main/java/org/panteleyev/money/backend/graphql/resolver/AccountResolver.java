/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.springframework.stereotype.Component;

@Component
public class AccountResolver implements GraphQLResolver<Account> {
    private final CategoryService categoryService;
    private final IconService iconService;
    private final CurrencyService currencyService;

    public AccountResolver(
            CategoryService categoryService,
            IconService iconService,
            CurrencyService currencyService
    ) {
        this.categoryService = categoryService;
        this.iconService = iconService;
        this.currencyService = currencyService;
    }

    public Category category(Account account) {
        return categoryService.get(account.categoryUuid()).orElseThrow();
    }

    public Icon icon(Account account) {
        return account.iconUuid() == null ?
                null : iconService.get(account.iconUuid()).orElseThrow();
    }

    public Currency currency(Account account) {
        return account.currencyUuid() == null ?
                null : currencyService.get(account.currencyUuid()).orElseThrow();
    }
}
