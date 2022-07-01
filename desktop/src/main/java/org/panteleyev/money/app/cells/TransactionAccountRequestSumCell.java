/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;

import java.math.RoundingMode;

public class TransactionAccountRequestSumCell extends TableCell<Transaction, Transaction> {
    private final Account account;

    public TransactionAccountRequestSumCell(Account account) {
        this.account = account;
    }

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (empty || transaction == null) {
            setText("");
        } else {
            getStyleClass().add(
                    transaction.accountDebitedUuid().equals(account.uuid()) ?
                            Styles.DEBIT : Styles.CREDIT
            );

            var amount = transaction.accountCreditedUuid().equals(account.uuid()) ?
                    transaction.getConvertedAmount() :
                    transaction.amount();

            setText(amount.setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
