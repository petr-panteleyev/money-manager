/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.model.Currency;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CurrencyQuery implements GraphQLQueryResolver {
    private final CurrencyService service;

    public CurrencyQuery(CurrencyService service) {
        this.service = service;
    }

    public List<Currency> currencies() {
        return service.getAll();
    }

    public Currency currency(UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Currency", uuid));
    }
}
