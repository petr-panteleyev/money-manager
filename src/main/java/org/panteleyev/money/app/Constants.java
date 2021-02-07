/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import java.util.function.Function;
import java.util.function.Supplier;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static org.panteleyev.money.app.MainWindowController.RB;

public interface Constants {
    String ELLIPSIS = "...";
    String COLON = ":";

    KeyCodeCombination SHORTCUT_C = new KeyCodeCombination(KeyCode.C, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_E = new KeyCodeCombination(KeyCode.E, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_F = new KeyCodeCombination(KeyCode.F, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_H = new KeyCodeCombination(KeyCode.H, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_K = new KeyCodeCombination(KeyCode.K, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_N = new KeyCodeCombination(KeyCode.N, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_O = new KeyCodeCombination(KeyCode.O, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_R = new KeyCodeCombination(KeyCode.R, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_T = new KeyCodeCombination(KeyCode.T, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_U = new KeyCodeCombination(KeyCode.U, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_DELETE = new KeyCodeCombination(KeyCode.DELETE, SHORTCUT_DOWN);
    KeyCodeCombination SHORTCUT_ALT_C = new KeyCodeCombination(KeyCode.C, SHORTCUT_DOWN, ALT_DOWN);

    Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };

    String ALL_TYPES_STRING = RB.getString("All_Types");

    Function<Category, Image> CATEGORY_TO_IMAGE = category -> IconManager.getImage(category.iconUuid());
    Function<Account, Image> ACCOUNT_TO_IMAGE = account -> IconManager.getImage(account.iconUuid());

    FileChooser.ExtensionFilter FILTER_XML_FILES = new FileChooser.ExtensionFilter(RB.getString("XML_Files"), "*.xml");
    FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter(RB.getString("All_Files"), "*.*");
}
