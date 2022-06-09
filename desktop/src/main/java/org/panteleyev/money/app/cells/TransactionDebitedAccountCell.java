/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.GlobalContext.cache;

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
