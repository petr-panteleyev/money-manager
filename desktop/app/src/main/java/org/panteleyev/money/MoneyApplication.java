/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.panteleyev.money.app.MainWindowController;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.panteleyev.money.app.GlobalContext.files;
import static org.panteleyev.money.app.GlobalContext.settings;

public class MoneyApplication extends Application {
    private final static Logger LOGGER = Logger.getLogger(MoneyApplication.class.getName());

    private final static String LOG_PROPERTIES = """
            handlers                                = java.util.logging.FileHandler

            java.util.logging.FileHandler.level     = ALL
            java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
            java.util.logging.FileHandler.pattern   = %FILE_PATTERN%
            java.util.logging.FileHandler.append    = true

            java.util.logging.SimpleFormatter.format = %1$tF %1$tk:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n
            """;

    private static MoneyApplication application;

    public MoneyApplication() {
        application = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        files().initialize();
        if (!files().lock()) {
            return;
        }

        settings().load();

        var logProperties = LOG_PROPERTIES.replace("%FILE_PATTERN%",
                files().getLogDirectory().resolve("MoneyManager.log").toString());
        try (var inputStream = new ByteArrayInputStream(logProperties.getBytes(UTF_8))) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }

        Thread.setDefaultUncaughtExceptionHandler((_, e) -> uncaughtException(e));

        new MainWindowController(primaryStage);

        primaryStage.show();
    }

    public static void uncaughtException(Throwable e) {
        LOGGER.log(Level.SEVERE, "Uncaught exception", e);
        Platform.runLater(() -> {
            var alert = new Alert(Alert.AlertType.ERROR, e.toString());
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        Application.launch(MoneyApplication.class, args);
    }

    public static String generateFileName(String prefix) {
        return prefix + "-" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static String generateFileName() {
        return generateFileName("Money");
    }

    public static void showDocument(String uri) {
        if (application != null) {
            application.getHostServices().showDocument(uri);
        }
    }
}
