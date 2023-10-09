/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction;

import javafx.scene.control.TableRow;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.Styles.GROUP_CELL;

class TransactionRow extends TableRow<Transaction> {
    @Override
    public void updateItem(Transaction item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(GROUP_CELL);

        if (item != null && !empty) {
            if (item.detailed()) {
                getStyleClass().add(GROUP_CELL);
            }
        }
    }
}
