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

    FileChooser.ExtensionFilter FILTER_XML_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_XML_FILES), "*.xml");
    FileChooser.ExtensionFilter FILTER_ZIP_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_ZIP_FILES), "*.zip");
    FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter(UI.getString(I18N_MISC_ALL_FILES), "*.*");

    ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
        ResourceBundle.getBundle(TransactionPredicateBundle.class.getCanonicalName());
}
