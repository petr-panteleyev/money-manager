/*
 * Copyright (c) 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.database;

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
            String s = keyEdit2.getText();
            keyEdit2.setText(UUID.randomUUID().toString());
            keyEdit2.setText(s);

            return ValidationResult.fromErrorIf(c, null, false);
        };

        Validator<String> v2 = (Control c, String value) -> {
            boolean equal = Objects.equals(keyEdit.getText(), keyEdit2.getText());
            return ValidationResult.fromErrorIf(c, null, !equal);
        };

        validation.registerValidator(keyEdit, v1);
        validation.registerValidator(keyEdit2, v2);
    }
}
