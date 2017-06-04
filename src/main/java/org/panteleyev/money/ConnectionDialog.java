/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.controlsfx.validation.ValidationResult;
import org.panteleyev.money.persistence.MySQLBuilder;
import org.panteleyev.utilities.fx.BaseDialog;
import java.util.ResourceBundle;

class ConnectionDialog extends BaseDialog<MySQLBuilder> implements Styles {
    private final ResourceBundle rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final TextField     hostEdit     = new TextField();
    private final TextField     userNameEdit = new TextField();
    private final PasswordField passwordEdit = new PasswordField();
    private final TextField     nameEdit     = new TextField();
    private final TextField     portEdit     = new TextField();
    private final CheckBox      autoConnectCheck = new CheckBox(rb.getString("connect.Dialog.autoCheck"));

    ConnectionDialog(boolean newConnection) {
        super(MainWindowController.DIALOGS_CSS);
        initialize(newConnection);
    }

    private void initialize(boolean newConnection) {
        setTitle(rb.getString("connection.Dialog.Title"));

        GridPane grid = new GridPane();
        grid.getStyleClass().add(GRID_PANE);

        int row = 0;

        grid.addRow(row++, new Label(rb.getString("label.Host")), hostEdit);
        grid.addRow(row++, new Label(rb.getString("label.Port")), portEdit);
        grid.addRow(row++, new Label(rb.getString("label.User")), userNameEdit);
        grid.addRow(row++, new Label(rb.getString("label.Password")), passwordEdit);
        grid.addRow(row++, new Label(rb.getString("label.Database")), nameEdit);
        grid.addRow(row, autoConnectCheck);

        hostEdit.setPrefColumnCount(20);
        GridPane.setColumnSpan(autoConnectCheck, 2);

        getDialogPane().setContent(grid);
        createDefaultButtons(rb);
        Platform.runLater(this::createValidationSupport);

        if (newConnection) {
            hostEdit.setText("localhost");
            portEdit.setText("3306");
        } else {
            hostEdit.setText(Options.getDatabaseHost());
            portEdit.setText(Integer.toString(Options.getDatabasePort()));
            userNameEdit.setText(Options.getDatabaseUser());
            passwordEdit.setText(Options.getDatabasePassword());
            nameEdit.setText(Options.getDatabaseName());
            autoConnectCheck.setSelected(Options.getAutoConnect());
        }

        setResultConverter((ButtonType b) -> {
            if (b == ButtonType.OK) {
                Options.putAutoConnect(autoConnectCheck.isSelected());

                return new MySQLBuilder()
                        .host(hostEdit.getText())
                        .port(Integer.parseInt(portEdit.getText()))
                        .user(userNameEdit.getText())
                        .password(passwordEdit.getText())
                        .name(nameEdit.getText());
            } else {
                return null;
            }
        });
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(hostEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(userNameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(portEdit, (Control control, String value) -> {
            boolean correct = false;
            try {
                correct = Integer.parseInt(value) > 0;
            } catch (Exception ex) {
                // do nothing
            }
            return ValidationResult.fromErrorIf(control, "Invalid port number", !correct);
        });
        validation.initInitialDecoration();
    }
}
