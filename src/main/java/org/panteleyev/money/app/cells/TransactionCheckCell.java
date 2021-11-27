/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionCheckCell extends TableCell<Transaction, Transaction> {
    private static final String CHECK_SYMBOL = "\u2714";

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setGraphic(null);

        if (empty || transaction == null || !transaction.checked()) {
            setText("");
        } else {
            setText(CHECK_SYMBOL);
        }
    }
}
