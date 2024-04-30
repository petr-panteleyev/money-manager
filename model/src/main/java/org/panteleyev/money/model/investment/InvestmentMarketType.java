/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.investment;

import java.util.Arrays;
import java.util.Objects;

public enum InvestmentMarketType {
    UNKNOWN("-"),
    STOCK_MARKET("Фондовый рынок");

    private final String title;

    InvestmentMarketType(String title) {
        this.title = title;
    }

    public static InvestmentMarketType fromTitle(String title) {
        return Arrays.stream(InvestmentMarketType.values())
                .filter(x -> Objects.equals(x.title, title))
                .findAny()
                .orElse(InvestmentMarketType.UNKNOWN);
    }
}
