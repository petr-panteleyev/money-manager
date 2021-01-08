/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Transaction;

public class TransactionCommentCell extends TableCell<Transaction, Transaction> {

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setText("");
        setGraphic(null);

        if (empty || transaction == null) {
            return;
        } else {
            setText(transaction.comment());
        }
    }
}
