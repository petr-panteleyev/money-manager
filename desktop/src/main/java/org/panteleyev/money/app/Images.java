/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.scene.image.Image;
import org.panteleyev.money.model.CardType;

import static org.panteleyev.money.app.icons.IconManager.ICON_SIZE;

public final class Images {
    private static final String BASE_PACKAGE = "org/panteleyev/money/images/";

    public static final Image APP_ICON = image("icon48.png");
    public static final Image WARNING = image("warning-16.png");
    public static final Image EMPTY = image("empty.png");
    public static final Image SEARCH = image("search.png");
    public static final Image VISA = image("visa.png");
    public static final Image MASTERCARD = image("mastercard.png");
    public static final Image AMEX = image("amex.png");
    public static final Image MIR = image("mir.png");
    public static final Image ATTACHMENT = image("attachment.png");

    public static Image getCardTypeIcon(CardType cardType) {
        return switch (cardType) {
            case VISA -> Images.VISA;
            case MASTERCARD -> Images.MASTERCARD;
            case AMEX -> Images.AMEX;
            case MIR -> Images.MIR;
            default -> null;
        };
    }

    private static Image image(String name) {
        return new Image(BASE_PACKAGE + name, ICON_SIZE, ICON_SIZE, true, true);
    }

    private Images() {
    }
}
