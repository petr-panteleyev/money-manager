/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.exchange;

import static org.panteleyev.money.model.exchange.ExchangeSecurityGroup.STOCK_BONDS;
import static org.panteleyev.money.model.exchange.ExchangeSecurityGroup.STOCK_ETF;
import static org.panteleyev.money.model.exchange.ExchangeSecurityGroup.STOCK_PPIF;
import static org.panteleyev.money.model.exchange.ExchangeSecurityGroup.STOCK_SHARES;

public enum ExchangeSecurityType {
    COMMON_SHARE(STOCK_SHARES, "Акция обыкновенная"),
    PREFERRED_SHARE(STOCK_SHARES, "Акция привилегированная "),
    OFZ_BOND(STOCK_BONDS, "Государственная облигация"),
    CORPORATE_BOND(STOCK_BONDS, "Корпоративная облигация"),
    EXCHANGE_BOND(STOCK_BONDS, "Биржевая облигация"),
    PUBLIC_PPIF(STOCK_PPIF, "Пай открытого ПИФа"),
    ETF_PPIF(STOCK_ETF, "ETF"),
    EXCHANGE_PPIF(STOCK_PPIF, "Пай биржевого ПИФа"),
    OTHER(ExchangeSecurityGroup.OTHER, "Другое");

    private final ExchangeSecurityGroup group;
    private final String title;

    ExchangeSecurityType(ExchangeSecurityGroup group, String title) {
        this.group = group;
        this.title = title;
    }

    public ExchangeSecurityGroup getGroup() {
        return group;
    }

    public String getTitle() {
        return title;
    }

    public static ExchangeSecurityType of(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        } else {
            try {
                return ExchangeSecurityType.valueOf(value.toUpperCase());
            } catch (Exception ex) {
                return OTHER;
            }
        }
    }
}
