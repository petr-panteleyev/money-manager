/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.text.Font;

public enum FontOption {
    CONTROLS_FONT,
    MENU_FONT,
    TABLE_CELL_FONT,
    DIALOG_LABEL_FONT;

    private Font font;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }
}
