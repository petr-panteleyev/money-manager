/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class TransactionTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {"CARD_PAYMENT", "Оплата по карте"},
                {"CASH_PURCHASE", "Покупка за наличные"},
                {"CHEQUE", "Чек"},
                {"TRANSFER", "Перевод"},
                {"DEPOSIT", "Депозит"},
                {"WITHDRAWAL", "Снятие наличных"},
                {"INTEREST", "Проценты"},
                {"INCOME", "Доход"},
                {"FEE", "Комиссия"},
                {"CACHIER", "Транзакция в банкомате"},
                {"DIVIDEND", "Дивиденды"},
                {"DIRECT_BILLING", "Прямое дебетование"},
                {"CHARGE", "Списание"},
                {"SALE", "Продажа"},
                {"REFUND", "Возврат"},
                {"UNDEFINED", "Неизвестно"},
        };
    }
}
