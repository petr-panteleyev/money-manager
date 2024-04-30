/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.investment;

import java.util.Arrays;
import java.util.Objects;

public enum InvestmentOperationType {
    UNKNOWN("-"),
    PURCHASE("Покупка"),
    SELL("Продажа");

    private final String title;

    InvestmentOperationType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static InvestmentOperationType fromTitle(String title) {
        return Arrays.stream(InvestmentOperationType.values())
                .filter(x -> Objects.equals(x.title,  title))
                .findAny()
                .orElse(InvestmentOperationType.UNKNOWN);
    }
}
