/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Card;

public class CardNumberCell extends TableCell<Card, Card> {
    @Override
    protected void updateItem(Card card, boolean empty) {
        super.updateItem(card, empty);

        setText("");
        setGraphic(null);

        if (empty || card == null || card.number().isBlank()) {
            return;
        }

        setText(card.number());
        setGraphic(IconManager.getCardImageView(card));
    }
}
