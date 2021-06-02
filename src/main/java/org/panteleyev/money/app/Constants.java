/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import java.util.function.Supplier;
import static org.panteleyev.money.app.MainWindowController.RB;

public interface Constants {
    Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };

    String ALL_TYPES_STRING = RB.getString("All_Types");

    FileChooser.ExtensionFilter FILTER_XML_FILES = new FileChooser.ExtensionFilter(RB.getString("XML_Files"), "*.xml");
    FileChooser.ExtensionFilter FILTER_ALL_FILES = new FileChooser.ExtensionFilter(RB.getString("All_Files"), "*.*");
}
