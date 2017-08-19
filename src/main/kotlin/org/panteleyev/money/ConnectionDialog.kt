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

package org.panteleyev.money

import javafx.application.Platform
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import org.controlsfx.validation.ValidationResult
import org.panteleyev.money.persistence.MySQLBuilder
import org.panteleyev.utilities.fx.BaseDialog
import java.util.ResourceBundle

class ConnectionDialog(newConnection : Boolean) : BaseDialog<MySQLBuilder>(MainWindowController.CSS_PATH) {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val hostEdit = TextField()
    private val userNameEdit = TextField()
    private val passwordEdit = PasswordField()
    private val nameEdit = TextField()
    private val portEdit = TextField()
    private val autoConnectCheck = CheckBox(rb.getString("connect.Dialog.autoCheck"))

    init {
        title = rb.getString("connection.Dialog.Title")

        dialogPane.content = GridPane().apply {
            styleClass.add(Styles.GRID_PANE)

            var row = 0
            addRow(row++, Label(rb.getString("label.Host")), hostEdit)
            addRow(row++, Label(rb.getString("label.Port")), portEdit)
            addRow(row++, Label(rb.getString("label.User")), userNameEdit)
            addRow(row++, Label(rb.getString("label.Password")), passwordEdit)
            addRow(row++, Label(rb.getString("label.Database")), nameEdit)
            addRow(row, autoConnectCheck)
        }

        hostEdit.prefColumnCount = 20
        GridPane.setColumnSpan(autoConnectCheck, 2)

        createDefaultButtons(rb)
        Platform.runLater { this.createValidationSupport() }

        if (newConnection) {
            hostEdit.text = "localhost"
            portEdit.text = "3306"
        } else {
            hostEdit.text = Options.databaseHost
            portEdit.text = Integer.toString(Options.databasePort)
            userNameEdit.text = Options.databaseUser
            passwordEdit.text = Options.databasePassword
            nameEdit.text = Options.databaseName
            autoConnectCheck.isSelected = Options.autoConnect
        }

        setResultConverter { b: ButtonType ->
            if (b == ButtonType.OK) {
                Options.autoConnect = autoConnectCheck.isSelected

                return@setResultConverter MySQLBuilder()
                        .host(hostEdit.text)
                        .port(Integer.parseInt(portEdit.text))
                        .user(userNameEdit.text)
                        .password(passwordEdit.text)
                        .name(nameEdit.text)
            } else {
                return@setResultConverter null
            }
        }
    }

    private fun createValidationSupport() {
        with (validation) {
            registerValidator(nameEdit) {
                control: Control, value: String ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
            }
            registerValidator(hostEdit) {
                control: Control, value: String ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
            }
            registerValidator(userNameEdit) {
                control: Control, value: String ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty())
            }
            registerValidator(portEdit) { control: Control, value: String ->
                var correct = false
                try {
                    correct = Integer.parseInt(value) > 0
                } catch (ex: Exception) {
                    // do nothing
                }

                ValidationResult.fromErrorIf(control, "Invalid port number", !correct)
            }
            initInitialDecoration()
        }
    }

}