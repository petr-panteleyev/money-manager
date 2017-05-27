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
 *
 */
package org.panteleyev.money;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.panteleyev.utilities.fx.BaseDialog;

class AboutDialog extends BaseDialog implements Styles, Images {
    private final static String APP_TITLE = "Money Manager";

    private final Label label_1 = new Label(APP_TITLE);
    private final Label label_2 = new Label("Copyright (c) 2016, 2017, Petr Panteleyev");

    AboutDialog() {
        super(MainWindowController.DIALOGS_CSS);
        initialize();
    }

    private void initialize() {
        setTitle(APP_TITLE);

        BorderPane pane = new BorderPane();

        ImageView icon = new ImageView(APP_ICON);
        icon.setFitWidth(48);
        icon.setFitHeight(48);

        GridPane grid = new GridPane();
        grid.getStyleClass().add(GRID_PANE);
        grid.addRow(0, new Label("Version:"), new Label("${version}"));
        grid.addRow(1, new Label("Build:"), new Label("${timestamp}"));

        VBox vBox = new VBox(10, label_1, label_2, grid);
        label_1.getStyleClass().add(ABOUT_APP_TITLE_LABEL);
        label_2.getStyleClass().add(ABOUT_LABEL);

        pane.setLeft(icon);
        pane.setCenter(vBox);

        BorderPane.setMargin(vBox, new Insets(0, 0, 0, 10));

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
}
