/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

public enum ExchangeSecurityGroup {
    OTHER("Другое"),
    STOCK_INDEX("Индексы"),
    STOCK_SHARES("Акции"),
    STOCK_BONDS("Облигации"),
    CURRENCY_SELT("Валюта"),
    FUTURES_FORTS("Фьючерсы"),
    FUTURES_OPTIONS("Опционы"),
    STOCK_DR("Депозитарные расписки"),
    STOCK_FOREIGN_SHARES("Иностранные ц.б."),
    STOCK_EUROBOND("Еврооблигации"),
    STOCK_PPIF("Паи ПИФов"),
    STOCK_ETF("Биржевые фонды"),
    CURRENCY_METAL("Драгоценные металлы"),
    STOCK_QNV("Квал. инвесторы"),
    STOCK_GCC("Клиринговые сертификаты участия"),
    STOCK_DEPOSIT("Депозиты с ЦК"),
    CURRENCY_FUTURES("Валютный фьючерс"),
    CURRENCY_INDICES("Валютные фиксинги"),
    STOCK_MORTGAGE("Ипотечный сертификат");

    private final String title;

    ExchangeSecurityGroup(String title) {
        this.title = title;
    }

    public static ExchangeSecurityGroup from(String value) {
        if (value == null || value.isBlank()) {
            return ExchangeSecurityGroup.OTHER;
        } else {
            try {
                return ExchangeSecurityGroup.valueOf(value.toUpperCase());
            } catch (Exception ex) {
                return OTHER;
            }
        }
    }
}
