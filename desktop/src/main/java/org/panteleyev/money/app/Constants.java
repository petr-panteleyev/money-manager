/*
 Copyright © 2020-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.util.function.Supplier;

import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ALL_TYPES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_HTML_FILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ZIP_FILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENTS;

public final class Constants {
    public static final Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };

    public static final String ALL_TYPES_STRING = UI.getString(I18N_MISC_ALL_TYPES);

    public static final FileChooser.ExtensionFilter FILTER_STATEMENT_FILES =
            new FileChooser.ExtensionFilter(UI.getString(I18N_WORD_STATEMENTS), "*.ofx", "*.html", "*.htm");
    public static final FileChooser.ExtensionFilter FILTER_ZIP_FILES =
            new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_ZIP_FILES), "*.zip");
    public static final FileChooser.ExtensionFilter FILTER_HTML_FILES =
            new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_HTML_FILES), "*.html");

    private Constants() {
    }
}
