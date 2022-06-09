/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionDayCell extends TableCell<Transaction, Transaction> {
    private final boolean fullDate;

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
