/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;

public enum ColorOption {
    // Transactions
    DEBIT(Color.RED),
    CREDIT(Color.GREEN),
    TRANSFER(Color.BLUE),
    // Statements
    STATEMENT_CHECKED(Color.web("abebc6")),
    STATEMENT_UNCHECKED(Color.web("f9e79f")),
    STATEMENT_MISSING(Color.web("f5b7b1"));

    public static final Color DEFAULT_CELL_TEXT_COLOR = Color.BLACK;

    private Color color;

    ColorOption(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public String getWebString() {
        return "#"
            + colorToHex(color.getRed())
            + colorToHex(color.getGreen())
            + colorToHex(color.getBlue());
    }

    private static String colorToHex(double c) {
        var intValue = (int) (c * 255);
        var s = Integer.toString(intValue, 16);
        if (intValue < 16) {
            return "0" + s;
        } else {
            return s;
        }
    }
}
