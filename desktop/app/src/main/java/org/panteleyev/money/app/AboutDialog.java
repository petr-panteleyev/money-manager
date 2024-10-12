/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.TimeZone;

import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;
import static org.panteleyev.money.app.Styles.STYLE_ABOUT_ICON;
import static org.panteleyev.money.app.Styles.STYLE_ABOUT_LABEL;

final class AboutDialog extends BaseDialog<Object> {

    private record BuildInformation(String version, String timestamp) {
        static BuildInformation load() {
            var bundle = ResourceBundle.getBundle("org.panteleyev.money.buildInfo");
            return new BuildInformation(
                    bundle.getString("version"),
                    bundle.getString("timestamp")
            );
        }
    }

    static final String APP_TITLE = "Money Manager";

    private static final ZoneId LOCAL_TIME_ZONE = TimeZone.getDefault().toZoneId();
    private static final DateTimeFormatter TIMESTAMP_PARSER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssVV");
    private static final DateTimeFormatter LOCAL_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private static final String RUNTIME = System.getProperty("java.vm.version") + " " + System.getProperty("os.arch");
    private static final String VM = System.getProperty("java.vm.name") + " by " + System.getProperty("java.vm.vendor");
    private static final BuildInformation BUILD = BuildInformation.load();

    AboutDialog(Controller owner) {
        super(owner, settings().getAboutDialogCssFileUrl());

        setHeaderText(APP_TITLE);
        var iconView = new ImageView(Images.APP_ICON);
        iconView.getStyleClass().add(STYLE_ABOUT_ICON);
        setGraphic(iconView);

        setTitle("About " + APP_TITLE);

        var aboutLabel = label(fxString(APP_TITLE, " ") + BUILD.version());
        aboutLabel.getStyleClass().add(STYLE_ABOUT_LABEL);

        var timestamp = ZonedDateTime.parse(BUILD.timestamp(), TIMESTAMP_PARSER)
                .withZoneSameInstant(LOCAL_TIME_ZONE);

        var vBox = vBox(BIG_SPACING,
                vBox(SMALL_SPACING,
                        aboutLabel,
                        label("Built on " + LOCAL_FORMATTER.format(timestamp))
                ),
                vBox(SMALL_SPACING,
                        label("Runtime version: " + RUNTIME),
                        label("VM: " + VM)
                ),
                vBox(SMALL_SPACING,
                        label("Copyright © 2017-" + LocalDate.now().getYear() + " Petr Panteleyev")
                )
        );

        getDialogPane().setContent(vBox);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK);
    }
}
