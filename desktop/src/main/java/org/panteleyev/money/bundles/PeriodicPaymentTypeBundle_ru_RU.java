/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

import static org.panteleyev.money.model.PeriodicPaymentType.AUTO_PAYMENT;
import static org.panteleyev.money.model.PeriodicPaymentType.CARD_PAYMENT;
import static org.panteleyev.money.model.PeriodicPaymentType.MANUAL_PAYMENT;

public class PeriodicPaymentTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
                {MANUAL_PAYMENT.name(), "Ручной платёж"},
                {AUTO_PAYMENT.name(), "Автоплатеж"},
                {CARD_PAYMENT.name(), "Платёж с карты"}
        };
    }
}
