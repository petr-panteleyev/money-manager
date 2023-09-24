/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Card;

import static org.panteleyev.money.app.GlobalContext.cache;

public class CardCategoryCell extends TableCell<Card, Card> {
    @Override
    protected void updateItem(Card card, boolean empty) {
        super.updateItem(card, empty);

        setText("");
        setGraphic(null);

        if (empty || card == null) {
            return;
        }

        cache().getAccount(card.accountUuid()).flatMap(account -> cache().getCategory(account.categoryUuid()))
                .ifPresent(category -> {
                    setText(category.name());
                    setGraphic(IconManager.getImageView(category.iconUuid()));
                });
    }
}
