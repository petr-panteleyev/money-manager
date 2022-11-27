/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.controller;

import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.CurrencyInput;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.model.Currency;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
public class CurrencyGraphQLController {
    private final CurrencyService service;

    public CurrencyGraphQLController(CurrencyService service) {
        this.service = service;
    }

    @QueryMapping
    public List<Currency> currencies() {
        return service.getAll();
    }

    @QueryMapping
    public Currency currency(@Argument UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Currency", uuid));
    }

    @MutationMapping
    public Currency createCurrency(@Argument CurrencyInput input) {
        var currency = new Currency.Builder()
                .symbol(input.symbol())
                .description(input.description())
                .formatSymbol(input.formatSymbol())
                .formatSymbolPosition(input.formatSymbolPosition())
                .showFormatSymbol(input.showFormatSymbol())
                .def(input.def())
                .rate(input.rate())
                .direction(input.direction())
                .useThousandSeparator(input.useThousandSeparator())
                .build();
        return service.put(currency)
                .orElseThrow(() -> new GraphQLCreateException("Currency"));
    }

    @MutationMapping
    public Currency updateCurrency(
            @Argument UUID uuid,
            @Argument CurrencyInput input
    ) {
        var builder = service.get(uuid)
                .map(Currency.Builder::new)
                .orElseThrow();

        builder.symbol(input.symbol())
                .description(input.description())
                .formatSymbol(input.formatSymbol())
                .formatSymbolPosition(input.formatSymbolPosition())
                .showFormatSymbol(input.showFormatSymbol())
                .def(input.def())
                .rate(input.rate())
                .direction(input.direction())
                .useThousandSeparator(input.useThousandSeparator());
        return service.put(builder.build())
                .orElseThrow(() -> new GraphQLUpdateException("Currency", uuid));
    }
}
