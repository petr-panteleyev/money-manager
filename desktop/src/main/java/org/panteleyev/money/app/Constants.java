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
import org.panteleyev.money.bundles.TransactionPredicateBundle;

import java.util.ResourceBundle;
import java.util.function.Supplier;

import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ALL_FILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ALL_TYPES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_XML_FILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ZIP_FILES;

public interface Constants {
    Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };

    String ALL_TYPES_STRING = UI.getString(I18N_MISC_ALL_TYPES);

    FileChooser.ExtensionFilter FILTER_XML_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_XML_FILES),
            "*.xml");
    FileChooser.ExtensionFilter FILTER_ZIP_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_ZIP_FILES),
            "*.zip");
    FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_ALL_FILES),
            "*.*");

    ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
            ResourceBundle.getBundle(TransactionPredicateBundle.class.getCanonicalName());
}
