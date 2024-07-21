/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model.exchange;

public enum ExchangeSecuritySplitType {
    SPLIT("Сплит"),
    REVERSE_SPLIT("Обратный сплит");

    private final String title;

    ExchangeSecuritySplitType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
