/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction.cells;

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
