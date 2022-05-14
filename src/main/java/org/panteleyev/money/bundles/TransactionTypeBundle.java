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

public class TransactionTypeBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
            {"CARD_PAYMENT", "Card Payment"},
            {"CASH_PURCHASE", "Cash Purchase"},
            {"CHEQUE", "Cheque"},
            {"TRANSFER", "Transfer"},
            {"DEPOSIT", "Deposit"},
            {"WITHDRAWAL", "Cash Withdrawal"},
            {"INTEREST", "Interest"},
            {"INCOME", "Income"},
            {"FEE", "Fee"},
            {"CACHIER", "Cashier"},
            {"DIVIDEND", "Dividend"},
            {"DIRECT_BILLING", "Direct Billing"},
            {"CHARGE", "Charge"},
            {"SALE", "Sale"},
            {"REFUND", "Refund"},
            {"UNDEFINED", "Undefined"},
        };
    }
}
