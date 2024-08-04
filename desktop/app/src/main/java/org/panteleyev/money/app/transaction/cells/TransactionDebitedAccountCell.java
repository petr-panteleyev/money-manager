/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction.cells;

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

        if (transaction.cardUuid() != null) {
            cache().getCard(transaction.cardUuid()).ifPresent(card -> {
                setText(card.number());
                setGraphic(IconManager.getCardImageView(card));
            });
        } else {
            cache().getAccount(transaction.accountDebitedUuid()).ifPresent(account -> {
                setText(account.name());
                setGraphic(IconManager.getAccountImageView(account));
            });
        }
    }
}
