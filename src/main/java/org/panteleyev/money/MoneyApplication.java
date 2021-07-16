/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.panteleyev.money.app.MainWindowController;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import static org.panteleyev.money.app.options.Options.options;

public class MoneyApplication extends Application {
    private final static Logger LOGGER = Logger.getLogger(MoneyApplication.class.getName());
    private final static String FORMAT_PROP = "java.util.logging.SimpleFormatter.format";
    private final static String FORMAT = "%1$tF %1$tk:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n";

    public static MoneyApplication application;

    @Override
    public void start(Stage primaryStage) throws Exception {
        options().initialize();
        options().loadSettings();
        options().generateCssFiles();

        application = this;

        var formatProperty = System.getProperty(FORMAT_PROP);
        if (formatProperty == null) {
            System.setProperty(FORMAT_PROP, FORMAT);
        }
        LogManager.getLogManager()
            .readConfiguration(MoneyApplication.class.getResourceAsStream("logger.properties"));

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> uncaughtException(e));

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
}
