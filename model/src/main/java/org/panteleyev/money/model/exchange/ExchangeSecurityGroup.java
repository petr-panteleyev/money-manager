/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.exchange;

public enum ExchangeSecurityGroup {
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
    STOCK_MORTGAGE("Ипотечный сертификат"),
    OTHER("Другое");

    private final String title;

    ExchangeSecurityGroup(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ExchangeSecurityGroup of(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        } else {
            try {
                return ExchangeSecurityGroup.valueOf(value.toUpperCase());
            } catch (Exception ex) {
                return OTHER;
            }
        }
    }
}
