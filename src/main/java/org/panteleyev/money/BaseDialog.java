/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.controlsfx.validation.ValidationSupport;

public class BaseDialog<R> extends Dialog<R> {
    private final String fxml;
    private final ResourceBundle bundle;

    protected final ValidationSupport validation = new ValidationSupport();


    public BaseDialog(String fxml, String bundlePath) {
        this.fxml = fxml;
        this.bundle = ResourceBundle.getBundle(bundlePath);
    }

    public Dialog<R> load() {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource(fxml),
            bundle
        );
        loader.setController(this);

        try {
            getDialogPane().setContent(loader.load());
            return this;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected void createDefaultButtons() {
        getDialogPane().getButtonTypes().addAll(
            ButtonType.OK,
            ButtonType.CANCEL);

        Button btOk = (Button) getDialogPane().lookupButton(ButtonType.OK);
        btOk.disableProperty().bind(validation.invalidProperty());

        Button btCancel = (Button) getDialogPane().lookupButton(ButtonType.CANCEL);
        btCancel.setText(bundle.getString("button.Cancel"));
    }
}
