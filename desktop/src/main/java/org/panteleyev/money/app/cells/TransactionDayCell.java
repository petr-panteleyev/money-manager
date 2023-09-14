/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

import java.time.format.DateTimeFormatter;

public class TransactionDayCell extends TableCell<Transaction, Transaction> {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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
                setText(DATE_FORMAT.format(transaction.transactionDate()));
            } else {
                setText(Integer.toString(transaction.transactionDate().getDayOfMonth()));
            }
        }
    }
}
