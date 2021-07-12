/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import java.util.Optional;

public enum ColorOption {
    // Transactions
    DEBIT(Color.RED),
    CREDIT(Color.GREEN),
    TRANSFER(Color.BLUE),
    // Statements
    STATEMENT_CHECKED(Color.web("abebc6")),
    STATEMENT_UNCHECKED(Color.web("f9e79f")),
    STATEMENT_MISSING(Color.web("f5b7b1"));

    private final Color defaultColor;

    ColorOption(Color defaultColor) {
        this.defaultColor = defaultColor;
    }

    public Color getDefaultColor() {
        return defaultColor;
    }

    public static Optional<ColorOption> of(String str) {
        try {
            return Optional.of(valueOf(str));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
