// Copyright © 2017-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.transaction.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Transaction;

import static org.panteleyev.money.app.GlobalContext.cache;

public class TransactionContactCell extends TableCell<Transaction, Transaction> {

    @Override
    protected void updateItem(Transaction transaction, boolean empty) {
        super.updateItem(transaction, empty);

        if (empty || transaction == null) {
            clear();
            return;
        }

        cache().getContact(transaction.contactUuid()).ifPresentOrElse(contact -> {
            var contactName = contact.name();
            var location = transaction.location();
            if (!contactName.isBlank() && !location.isBlank()) {
                contactName += ", " + location;
            }
            setText(contactName);
            setGraphic(IconManager.getImageView(contact.iconUuid()));
        }, this::clear);
    }

    private void clear() {
        setText("");
        setGraphic(null);
    }
}
