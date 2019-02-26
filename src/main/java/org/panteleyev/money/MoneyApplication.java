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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MoneyApplication extends Application {
    private final static Logger LOGGER = Logger.getLogger(MoneyApplication.class.getName());
    private final static String FORMAT_PROP = "java.util.logging.SimpleFormatter.format";
    private final static String FORMAT = "%1$tF %1$tk:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n";

    public static MoneyApplication application;

    @Override
    public void start(Stage primaryStage) throws Exception {
        application = this;

        if (initLogDirectory()) {
            var formatProperty = System.getProperty(FORMAT_PROP);
            if (formatProperty == null) {
                System.setProperty(FORMAT_PROP, FORMAT);
            }
            LogManager.getLogManager()
                .readConfiguration(MoneyApplication.class.getResourceAsStream("logger.properties"));
        }

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> uncaughtException(e));

        new MainWindowController(primaryStage);

        primaryStage.show();
    }

    public static void uncaughtException(Throwable e) {
        LOGGER.log(Level.SEVERE, "Uncaught exception", e);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.toString());
            alert.showAndWait();
        });
    }

    private static boolean initLogDirectory() {
        var optionsDir = Options.getSettingsDirectory();
        var logDir = new File(optionsDir, "logs");

        return logDir.exists() ? logDir.isDirectory() : logDir.mkdir();
    }

    public static void main(String[] args) {
        Application.launch(MoneyApplication.class, args);
    }

    static String generateFileName() {
        return "Money-" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
