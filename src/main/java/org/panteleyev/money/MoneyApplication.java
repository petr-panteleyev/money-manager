/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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

import static org.panteleyev.money.app.GlobalContext.files;
import static org.panteleyev.money.app.GlobalContext.settings;

public class MoneyApplication extends Application {
    private final static Logger LOGGER = Logger.getLogger(MoneyApplication.class.getName());
    private final static String FORMAT_PROP = "java.util.logging.SimpleFormatter.format";
    private final static String FORMAT = "%1$tF %1$tk:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n";

    private static MoneyApplication application;

    public MoneyApplication() {
        application = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        files().initialize();
        settings().load();

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

    public static void showDocument(String uri) {
        if (application != null) {
            application.getHostServices().showDocument(uri);
        }
    }
}
