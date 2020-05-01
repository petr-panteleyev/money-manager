package org.panteleyev.money.app.cells;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Transaction;
import static org.panteleyev.money.persistence.DataCache.cache;

public class TransactionDebitedAccountCell extends TableCell<Transaction, Transaction> {
    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setText("");
        setGraphic(null);

        if (empty || transaction == null) {
            return;
        }

        cache().getAccount(transaction.accountDebitedUuid()).ifPresent(account -> {
            setText(account.name());
            setGraphic(IconManager.getAccountImageView(account));
        });
    }
}
