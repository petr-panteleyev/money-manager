/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Transaction;
import static org.panteleyev.money.app.GlobalContext.cache;

public class TransactionCreditedAccountCell extends TableCell<Transaction, Transaction> {

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setText("");
        setGraphic(null);

        if (empty || transaction == null) {
            return;
        }

        cache().getAccount(transaction.accountCreditedUuid()).ifPresent(account -> {
            setText(account.name());
            setGraphic(IconManager.getAccountImageView(account));
        });
    }
}
