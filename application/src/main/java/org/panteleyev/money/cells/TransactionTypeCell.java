package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionTypeCell extends TableCell<Transaction, Transaction> {
    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);
        setText(empty || transaction == null ? "" : transaction.getTransactionType().getTypeName());
    }
}
