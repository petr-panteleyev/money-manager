/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public sealed interface MoneyRecord
        permits Account, Card, Category, Contact, Currency, Icon, Transaction, MoneyDocument, PeriodicPayment, ExchangeSecurity {

    UUID uuid();

    long created();

    long modified();

    static BigDecimal normalize(BigDecimal value, BigDecimal defaultValue) {
        value = value == null ? defaultValue : value;
        return value.setScale(6, RoundingMode.HALF_UP);
    }

    static String normalize(String value) {
        return value == null ? "" : value;
    }
}
