/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.settings;

import javafx.scene.paint.Color;
import java.util.Optional;

public enum ColorName {
    // Transactions
    DEBIT(Color.RED),
    CREDIT(Color.GREEN),
    TRANSFER(Color.BLUE),
    // Statements
    STATEMENT_CHECKED(Color.web("abebc6")),
    STATEMENT_UNCHECKED(Color.web("f9e79f")),
    STATEMENT_MISSING(Color.web("f5b7b1"));

    private final Color defaultColor;

    ColorName(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public static Optional<ColorName> of(String str) {
        try {
            return Optional.of(valueOf(str));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
