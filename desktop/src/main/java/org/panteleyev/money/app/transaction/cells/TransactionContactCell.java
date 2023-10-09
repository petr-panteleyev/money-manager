/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.GlobalContext.cache;

public class TransactionContactCell extends TableCell<Transaction, Transaction> {

    @Override
    public void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        setText("");
        setGraphic(null);

        if (empty || transaction == null) {
            return;
        }

        cache().getContact(transaction.contactUuid()).ifPresent(contact -> {
            setText(contact.name());
            setGraphic(IconManager.getImageView(contact.iconUuid()));
        });
    }
}
