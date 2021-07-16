/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import java.util.Optional;

public enum FontOption {
    CONTROLS_FONT,
    MENU_FONT,
    TABLE_CELL_FONT,
    DIALOG_LABEL_FONT;

    public static Optional<FontOption> of(String str) {
        try {
            return Optional.of(valueOf(str));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
