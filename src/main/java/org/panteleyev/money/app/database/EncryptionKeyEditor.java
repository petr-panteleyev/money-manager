/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.database;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.util.Objects;
import java.util.UUID;

final class EncryptionKeyEditor extends VBox {
    private final PasswordField keyEdit = new PasswordField();
    private final PasswordField keyEdit2 = new PasswordField();
    private final ValidationSupport validation = new ValidationSupport();

    EncryptionKeyEditor() {
        getChildren().addAll(keyEdit, keyEdit2);

        VBox.setMargin(keyEdit, new Insets(5.0, 5.0, 5.0, 5.0));
        VBox.setMargin(keyEdit2, new Insets(5.0, 5.0, 5.0, 5.0));

        Platform.runLater(this::createValidationSupport);
    }

    String getEncryptionKey() {
        return keyEdit.getText();
    }

    ValidationSupport getValidation() {
        return validation;
    }

    void setEncryptionKey(String encryptionKey) {
        keyEdit.setText(encryptionKey);
        keyEdit2.setText(encryptionKey);
    }

    private void createValidationSupport() {
        Validator<String> v1 = (Control c, String value) -> {
            // Main password invalidates repeated password
            var s = keyEdit2.getText();
            keyEdit2.setText(UUID.randomUUID().toString());
            keyEdit2.setText(s);

            return ValidationResult.fromErrorIf(c, null, false);
        };

        Validator<String> v2 = (Control c, String value) -> {
            var equal = Objects.equals(keyEdit.getText(), keyEdit2.getText());
            return ValidationResult.fromErrorIf(c, null, !equal);
        };

        validation.registerValidator(keyEdit, v1);
        validation.registerValidator(keyEdit2, v2);
    }
}
