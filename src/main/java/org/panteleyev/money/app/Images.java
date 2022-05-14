/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
