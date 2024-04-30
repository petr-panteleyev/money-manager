/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.investment;

import java.util.Arrays;
import java.util.Objects;

public enum InvestmentDealType {
    UNKNOWN("-"),
    NORMAL("Обычная");

    private final String title;

    InvestmentDealType(String title) {
        this.title = title;
    }

    public static InvestmentDealType fromTitle(String title) {
        return Arrays.stream(InvestmentDealType.values())
                .filter(x -> Objects.equals(x.title, title))
                .findAny()
                .orElse(InvestmentDealType.UNKNOWN);
    }
}
