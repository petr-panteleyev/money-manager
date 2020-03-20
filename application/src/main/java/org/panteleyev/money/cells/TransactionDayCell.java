package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionDayCell extends TableCell<Transaction, Transaction> {
    private boolean fullDate;

    public TransactionDayCell(boolean fullDate) {
        this.fullDate = fullDate;
    }

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        if (empty || transaction == null) {
            setText("");
        } else {
            if (fullDate) {
                setText(String.format("%02d.%02d.%04d",
                    transaction.day(), transaction.month(), transaction.year()));
            } else {
                setText(Integer.toString(transaction.day()));
            }
        }
    }
}
