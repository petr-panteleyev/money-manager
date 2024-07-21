/*
 Copyright © 2020-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

public final class Constants {
    public static final Supplier<TextField> SEARCH_FIELD_FACTORY = () -> {
        var textField = TextFields.createClearableTextField();
        ((CustomTextField) textField).setLeft(new ImageView(Images.SEARCH));
        return textField;
    };

    public static final FileChooser.ExtensionFilter FILTER_STATEMENT_FILES =
            new FileChooser.ExtensionFilter("Выписки", "*.ofx", "*.html", "*.htm");
    public static final FileChooser.ExtensionFilter FILTER_ZIP_FILES =
            new FileChooser.ExtensionFilter("Файлы ZIP", "*.zip");
    public static final FileChooser.ExtensionFilter FILTER_HTML_FILES =
            new FileChooser.ExtensionFilter("Файлы HTML", "*.html");
    public static final FileChooser.ExtensionFilter FILTER_SBER_ONLINE_BROKER_DEALS =
            new FileChooser.ExtensionFilter("Брокерские сделки - Сбербанк-Онлайн", "*.xlsx");
    public static final FileChooser.ExtensionFilter FILTER_SBER_ONLINE_BROKER_DEALS_HTML =
            new FileChooser.ExtensionFilter("Брокерские сделки - Сбербанк", "*.html");
    public static final FileChooser.ExtensionFilter FILTER_RAIF_ONLINE_BROKER_DEALS =
            new FileChooser.ExtensionFilter("Брокерские сделки - Raiffeisen", "*.xlsx");

    public static final DateTimeFormatter FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Constants() {
    }
}
