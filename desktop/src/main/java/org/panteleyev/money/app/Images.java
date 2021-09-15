/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.image.Image;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Icon;

public interface Images {
    Image APP_ICON = new Image("org/panteleyev/money/images/icon48.png");
    Image WARNING = new Image("org/panteleyev/money/images/warning-16.png");
    Image EMPTY = new Image("org/panteleyev/money/images/empty.png");
    Image SEARCH = new Image("org/panteleyev/money/images/search.png");
    Image VISA = new Image("org/panteleyev/money/images/visa.png",
        Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image MASTERCARD = new Image("org/panteleyev/money/images/mastercard.png",
        Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image AMEX = new Image("org/panteleyev/money/images/amex.png",
        Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image MIR = new Image("org/panteleyev/money/images/mir.png",
        Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);

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
