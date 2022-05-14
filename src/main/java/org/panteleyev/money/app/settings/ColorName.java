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
