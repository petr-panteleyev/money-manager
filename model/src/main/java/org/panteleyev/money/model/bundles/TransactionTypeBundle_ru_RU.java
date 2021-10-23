/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model.bundles;

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
