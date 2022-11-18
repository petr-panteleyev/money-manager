/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.CurrencyInput;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.model.Currency;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrencyMutation implements GraphQLMutationResolver {
    private final CurrencyService service;

    public CurrencyMutation(CurrencyService service) {
        this.service = service;
    }

    public Currency createCurrency(CurrencyInput input) {
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

    public Currency updateCurrency(UUID uuid, CurrencyInput input) {
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
