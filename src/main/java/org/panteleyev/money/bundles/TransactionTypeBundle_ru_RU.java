/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
