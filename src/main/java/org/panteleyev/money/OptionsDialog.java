/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.panteleyev.money;

import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.panteleyev.utilities.fx.BaseDialog;
import java.util.ResourceBundle;

class OptionsDialog extends BaseDialog<ButtonType> {
    private final ResourceBundle rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final ChoiceBox<Integer> autoCompleteLength =
            new ChoiceBox<>(FXCollections.observableArrayList(2, 3, 4, 5));
    private final CheckBox autoConnectCheck = new CheckBox(rb.getString("connect.Dialog.autoCheck"));

    OptionsDialog() {
        super(MainWindowController.DIALOGS_CSS);
        setTitle(rb.getString("options.Dialog.Title"));
        createDefaultButtons(rb);

        GridPane pane = new GridPane();
        pane.getStyleClass().add(Styles.GRID_PANE);

        pane.addRow(0, new Label(rb.getString("options.Dialog.Prefix.Length")), autoCompleteLength);
        pane.addRow(1, autoConnectCheck);
        GridPane.setColumnSpan(autoConnectCheck, 2);

        getDialogPane().setContent(pane);

        initControls();

        setResultConverter((ButtonType param) -> {
            if (param == ButtonType.OK) {
                Options.setAutoCompleteLength(autoCompleteLength.getValue());
                Options.putAutoConnect(autoConnectCheck.isSelected());
            }
            return param;
        });
    }

    private void initControls() {
        autoCompleteLength.getSelectionModel().select(Integer.valueOf(Options.getAutoCompleteLength()));
        autoConnectCheck.setSelected(Options.getAutoConnect());
    }
}
