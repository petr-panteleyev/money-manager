package org.panteleyev.money.app.cells;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableRow;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.StatementRecord;
import static org.panteleyev.money.app.Styles.STATEMENT_ALL_CHECKED;
import static org.panteleyev.money.app.Styles.STATEMENT_NOT_CHECKED;
import static org.panteleyev.money.app.Styles.STATEMENT_NOT_FOUND;

public class StatementRow extends TableRow<StatementRecord> {
    @Override
    public void updateItem(StatementRecord item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(STATEMENT_NOT_FOUND, STATEMENT_NOT_CHECKED, STATEMENT_ALL_CHECKED);
        if (item == null || empty) {
            return;
        }

        var transactions = item.getTransactions();

        if (transactions.isEmpty()) {
            getStyleClass().add(STATEMENT_NOT_FOUND);
        } else {
            if (transactions.stream().allMatch(Transaction::checked)) {
                getStyleClass().add(STATEMENT_ALL_CHECKED);
            } else {
                getStyleClass().add(STATEMENT_NOT_CHECKED);
            }
        }
    }
}
