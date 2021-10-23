/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model.bundles;

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
