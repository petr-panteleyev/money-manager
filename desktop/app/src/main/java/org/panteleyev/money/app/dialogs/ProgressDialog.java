/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;

public class ProgressDialog extends BaseDialog<Object> {
    private final TextArea textArea = new TextArea();
    private final Button closeButton;

    public ProgressDialog(Controller owner, String title) {
        super(owner);
        setTitle(title);
        getDialogPane().setContent(new BorderPane(textArea));
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        closeButton = (Button) getDialogPane().lookupButton(ButtonType.CLOSE);
    }

    public void disableClose(boolean disable) {
        closeButton.setDisable(disable);
    }

    public void append(String text) {
        Platform.runLater(() -> textArea.appendText(text));
    }
}
