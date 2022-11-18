/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.model.Account;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class AccountQuery implements GraphQLQueryResolver {
    private final AccountService service;

    public AccountQuery(AccountService service) {
        this.service = service;
    }

    public List<Account> accounts() {
        return service.getAll();
    }

    public Account account(UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Account", uuid));
    }
}
