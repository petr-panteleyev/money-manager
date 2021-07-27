/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import java.time.LocalDate;
import java.util.ResourceBundle;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;
import static org.panteleyev.money.app.Styles.STYLE_ABOUT_LABEL;

final class AboutDialog extends BaseDialog<Object> {

    private static record BuildInformation(String version, String timestamp) {
        static BuildInformation load() {
            var bundle = ResourceBundle.getBundle("org.panteleyev.money.buildInfo");
            return new BuildInformation(
                bundle.getString("version"),
                bundle.getString("timestamp")
            );
        }
    }

    static final String APP_TITLE = "Money Manager";

    private static final String RUNTIME = System.getProperty("java.vm.version") + " " + System.getProperty("os.arch");
    private static final String VM = System.getProperty("java.vm.name") + " by " + System.getProperty("java.vm.vendor");
    private static final BuildInformation BUILD = BuildInformation.load();

    AboutDialog(Controller owner) {
        super(owner, settings().getAboutDialogCssFileUrl());

        setHeaderText(APP_TITLE);
        setGraphic(new ImageView(Images.APP_ICON));

        setTitle("About " + APP_TITLE);

        var aboutLabel = label(fxString(APP_TITLE, " ") + BUILD.version());
        aboutLabel.getStyleClass().add(STYLE_ABOUT_LABEL);

        var vBox = vBox(BIG_SPACING,
            vBox(SMALL_SPACING,
                aboutLabel,
                label("Built on " + BUILD.timestamp())
            ),
            vBox(SMALL_SPACING,
                label("Runtime version: " + RUNTIME),
                label("VM: " + VM)
            ),
            vBox(SMALL_SPACING,
                label("Copyright (c) 2016, " + LocalDate.now().getYear() + ", Petr Panteleyev")
            )
        );

        getDialogPane().setContent(vBox);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }
}
