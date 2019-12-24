/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.panteleyev.commons.fx.BaseDialog;
import java.util.ResourceBundle;
import static org.panteleyev.commons.fx.FXFactory.newLabel;

final class AboutDialog extends BaseDialog {
    static final String APP_TITLE = "Money Manager";
    private static final String COPYRIGHT = "Copyright (c) 2016, 2019, Petr Panteleyev";
    private static final String BUILD_INFO = "org.panteleyev.money.res.buildInfo";

    AboutDialog() {
        super(MainWindowController.CSS_PATH);

        setTitle(APP_TITLE);

        var icon = new ImageView(Images.APP_ICON);
        icon.setFitHeight(48);
        icon.setFitWidth(48);

        var rb = ResourceBundle.getBundle(BUILD_INFO);

        var grid = new GridPane();
        grid.getStyleClass().add(Styles.GRID_PANE);
        grid.addRow(0, new Label("Version:"), newLabel(rb, "version"));
        grid.addRow(1, new Label("Build:"), newLabel(rb, "timestamp"));

        var appLabel = new Label(APP_TITLE);
        appLabel.getStyleClass().add(Styles.ABOUT_APP_TITLE_LABEL);

        var copyrightLabel = new Label(COPYRIGHT);
        copyrightLabel.getStyleClass().add(Styles.ABOUT_LABEL);

        var vBox = new VBox(10, appLabel, copyrightLabel, grid);

        var pane = new BorderPane();
        pane.setLeft(icon);
        pane.setCenter(vBox);

        BorderPane.setMargin(vBox, new Insets(0.0, 0.0, 0.0, 10.0));

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
}
