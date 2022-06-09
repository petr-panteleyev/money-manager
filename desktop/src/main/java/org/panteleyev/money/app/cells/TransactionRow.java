/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableRow;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.Styles.GROUP_CELL;

public class TransactionRow extends TableRow<Transaction> {
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
