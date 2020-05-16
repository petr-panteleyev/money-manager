/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import java.util.function.Supplier;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;

public interface Constants {
    String ELLIPSIS = "...";
    String COLON = ":";

    KeyCodeCombination SHORTCUT_C = new KeyCodeCombination(KeyCode.C, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_E = new KeyCodeCombination(KeyCode.E, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_F = new KeyCodeCombination(KeyCode.F, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_H = new KeyCodeCombination(KeyCode.H, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_K = new KeyCodeCombination(KeyCode.K, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_N = new KeyCodeCombination(KeyCode.N, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_T = new KeyCodeCombination(KeyCode.T, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_U = new KeyCodeCombination(KeyCode.U, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE, SHORTCUT_DOWN);

    Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };
}
