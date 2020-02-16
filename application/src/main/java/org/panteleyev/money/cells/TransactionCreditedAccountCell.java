package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.icons.IconManager;
import org.panteleyev.money.model.Transaction;
import static org.panteleyev.money.persistence.DataCache.cache;

public class TransactionCreditedAccountCell extends TableCell<Transaction, Transaction> {

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setText("");
        setGraphic(null);

        if (empty || transaction == null) {
            return;
        }

        cache().getAccount(transaction.getAccountCreditedUuid()).ifPresent(account -> {
            setText(account.getName());
            setGraphic(IconManager.getAccountImageView(account));
        });
    }
}
