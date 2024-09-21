/*
 Copyright © 2023-2024 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.exchange;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;

import java.util.Objects;

import static org.panteleyev.fx.FxFactory.textField;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;

class SecurityCodeDialog extends BaseDialog<String> {
    private final ValidationSupport validation = new ValidationSupport();
    private final TextField codeField = textField(20);

    public SecurityCodeDialog(Controller owner) {
        super(owner, settings().getDialogCssFileUrl());

        setTitle("Код ценной бумаги");

        getDialogPane().setContent(
                new BorderPane(codeField)
        );

        setResultConverter((ButtonType b) -> b == ButtonType.OK ? codeField.getText() : null);
        createDefaultButtons(UI, validation.invalidProperty());

        Platform.runLater(codeField::requestFocus);
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(codeField, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty() || notUnique(value)));

        validation.initInitialDecoration();
    }

    private boolean notUnique(String secId) {
        return cache().getExchangeSecurities().stream()
                .anyMatch(s -> Objects.equals(s.secId(), secId));
    }
}
