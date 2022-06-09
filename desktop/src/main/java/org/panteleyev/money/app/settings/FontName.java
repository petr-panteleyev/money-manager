/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
