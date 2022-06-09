/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
