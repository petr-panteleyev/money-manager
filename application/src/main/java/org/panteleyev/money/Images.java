package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.image.Image;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Icon;

public interface Images {
    Image APP_ICON = new Image("org/panteleyev/money/res/icon48.png");
    Image WARNING = new Image("org/panteleyev/money/res/warning-16.png");
    Image EMPTY = new Image("org/panteleyev/money/res/empty.png");
    Image SEARCH = new Image("org/panteleyev/money/res/search.png");
    Image VISA = new Image("org/panteleyev/money/res/visa.png", Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image MASTERCARD = new Image("org/panteleyev/money/res/mastercard.png", Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image AMEX = new Image("org/panteleyev/money/res/amex.png", Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);
    Image MIR = new Image("org/panteleyev/money/res/mir.png", Icon.ICON_SIZE, Icon.ICON_SIZE, true, true);

    static Image getCardTypeIcon(CardType cardType) {
        switch (cardType) {
            case VISA:
                return Images.VISA;
            case MASTERCARD:
                return Images.MASTERCARD;
            case AMEX:
                return Images.AMEX;
            case MIR:
                return Images.MIR;
            default:
                return null;
        }
    }
}
