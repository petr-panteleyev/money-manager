/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;

import java.math.RoundingMode;

public class TransactionSumCell extends TableCell<Transaction, Transaction> {
    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT, Styles.TRANSFER);

        if (empty || transaction == null) {
            setText("");
        } else {
            if (transaction.accountCreditedType() == CategoryType.EXPENSES) {
                getStyleClass().add(Styles.DEBIT);
            } else if (transaction.accountCreditedType() == transaction.accountDebitedType()) {
                getStyleClass().add(Styles.TRANSFER);
            } else {
                getStyleClass().add(Styles.CREDIT);
            }

            setText(transaction.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
