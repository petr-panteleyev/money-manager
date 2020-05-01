package org.panteleyev.money.app.cells;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import org.panteleyev.money.model.CardType;
import static org.panteleyev.money.app.Images.getCardTypeIcon;

public class CardTypeComboBoxCell extends ListCell<CardType> {
    @Override
    protected void updateItem(CardType cardType, boolean empty) {
        super.updateItem(cardType, empty);

        if (empty || cardType == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(cardType.toString());
            setGraphic(new ImageView(getCardTypeIcon(cardType)));
        }
    }
}
