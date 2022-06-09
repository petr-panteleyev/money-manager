/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.scene.image.Image;
import org.panteleyev.money.model.CardType;

import static org.panteleyev.money.app.icons.IconManager.ICON_SIZE;

public interface Images {
    Image APP_ICON = new Image("org/panteleyev/money/images/icon48.png");
    Image WARNING = new Image("org/panteleyev/money/images/warning-16.png");
    Image EMPTY = new Image("org/panteleyev/money/images/empty.png");
    Image SEARCH = new Image("org/panteleyev/money/images/search.png");
    Image VISA = new Image("org/panteleyev/money/images/visa.png", ICON_SIZE, ICON_SIZE, true, true);
    Image MASTERCARD = new Image("org/panteleyev/money/images/mastercard.png", ICON_SIZE, ICON_SIZE, true, true);
    Image AMEX = new Image("org/panteleyev/money/images/amex.png", ICON_SIZE, ICON_SIZE, true, true);
    Image MIR = new Image("org/panteleyev/money/images/mir.png", ICON_SIZE, ICON_SIZE, true, true);
    Image ATTACHMENT = new Image("org/panteleyev/money/images/attachment.png", ICON_SIZE, ICON_SIZE, true, true);

    static Image getCardTypeIcon(CardType cardType) {
        return switch (cardType) {
            case VISA -> Images.VISA;
            case MASTERCARD -> Images.MASTERCARD;
            case AMEX -> Images.AMEX;
            case MIR -> Images.MIR;
            default -> null;
        };
    }
}
