/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Card;

import static org.panteleyev.money.app.GlobalContext.cache;

public class CardAccountCell extends TableCell<Card, Card> {
    @Override
    protected void updateItem(Card card, boolean empty) {
        super.updateItem(card, empty);

        setText("");
        setGraphic(null);

        if (empty || card == null) {
            return;
        }

        var account = cache().getAccount(card.accountUuid()).orElseThrow();
        setText(account.name());
    }
}
