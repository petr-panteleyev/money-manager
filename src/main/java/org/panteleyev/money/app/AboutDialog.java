/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import java.time.LocalDate;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.Bundles.BUILD_INFO_BUNDLE;
import static org.panteleyev.money.app.options.Options.options;

final class AboutDialog extends BaseDialog {
    static final String APP_TITLE = "Money Manager";
    private static final String COPYRIGHT = String.format(
        "Copyright © 2016, %d, Petr Panteleyev", LocalDate.now().getYear());

    AboutDialog(Controller owner) {
        super(owner, options().getDialogCssFileUrl());

        setTitle(APP_TITLE);

        var icon = new ImageView(Images.APP_ICON);
        icon.setFitHeight(48);
        icon.setFitWidth(48);

        var grid = new GridPane();
        grid.getStyleClass().add(Styles.GRID_PANE);
        grid.addRow(0, label("Version:"), label(fxString(BUILD_INFO_BUNDLE, "version")));
        grid.addRow(1, label("Build:"), label(fxString(BUILD_INFO_BUNDLE, "timestamp")));
        grid.addRow(2, label("Java:"), label(
            String.format("%s by %s",
                System.getProperty("java.version"),
                System.getProperty("java.vendor")
            ))
        );

        var appLabel = label(APP_TITLE);
        appLabel.getStyleClass().add(Styles.ABOUT_APP_TITLE_LABEL);

        var copyrightLabel = label(COPYRIGHT);
        copyrightLabel.getStyleClass().add(Styles.ABOUT_LABEL);

        var vBox = new VBox(10, appLabel, copyrightLabel, grid);
        var pane = new BorderPane(vBox, null, null, null, icon);

        BorderPane.setMargin(vBox, new Insets(0.0, 0.0, 0.0, 10.0));

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().add(ButtonType.OK);
    }
}
