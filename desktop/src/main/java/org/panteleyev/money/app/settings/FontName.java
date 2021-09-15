/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.settings;

import java.util.Optional;

public enum FontName {
    CONTROLS_FONT,
    MENU_FONT,
    TABLE_CELL_FONT,
    DIALOG_LABEL_FONT;

    public static Optional<FontName> of(String str) {
        try {
            return Optional.of(valueOf(str));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
