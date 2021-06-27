/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.MoneyApplication;
import org.panteleyev.money.app.TemplateEngine;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.prefs.Preferences;
import static java.util.Map.entry;
import static javafx.application.Platform.runLater;
import static org.panteleyev.money.app.TemplateEngine.templateEngine;

public final class Options {
    private static final double DEFAULT_WIDTH = 1024.0;
    private static final double DEFAULT_HEIGHT = 768.0;
    private static final int AUTO_COMPLETE_LENGTH = 3;
    private static final String OPTIONS_DIRECTORY = ".money-manager";

    private static final String DEFAULT_FONT_FAMILY = "System";
    private static final String DEFAULT_FONT_STYLE = "Normal Regular";
    private static final double DEFAULT_FONT_SIZE = 12;

    private File mainCssFile;
    private File dialogCssFile;
    private File aboutDialogCssFile;

    private static final Options OPTIONS = new Options();

    public static Options options() {
        return OPTIONS;
    }

    private Options() {
    }

    public void initialize() {
        var settingsDirectory = initDirectory(
            new File(System.getProperty("user.home") + File.separator + OPTIONS_DIRECTORY),
            "Options"
        );

        initDirectory(
            new File(settingsDirectory, "logs"),
            "Logs"
        );

        mainCssFile = new File(settingsDirectory, "main.css");
        dialogCssFile = new File(settingsDirectory, "dialog.css");
        aboutDialogCssFile = new File(settingsDirectory, "about-dialog.css");
    }

    private static File initDirectory(File dir, String name) {
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException(name + " directory cannot be opened/created");
            }
        } else {
            if (!dir.isDirectory()) {
                throw new RuntimeException(name + " directory cannot be opened/created");
            }
        }
        return dir;
    }

    public void generateCssFiles() {
        var dataModel = Map.ofEntries(
            entry("debitColor", ColorOption.DEBIT.getWebString()),
            entry("creditColor", ColorOption.CREDIT.getWebString()),
            entry("transferColor", ColorOption.TRANSFER.getWebString()),
            entry("controlsFontFamily", FontOption.CONTROLS_FONT.getFont().getFamily()),
            entry("controlsFontSize", (int) FontOption.CONTROLS_FONT.getFont().getSize()),
            entry("menuFontFamily", FontOption.MENU_FONT.getFont().getFamily()),
            entry("menuFontSize", (int) FontOption.MENU_FONT.getFont().getSize()),
            entry("tableCellFontFamily", FontOption.TABLE_CELL_FONT.getFont().getFamily()),
            entry("tableCellFontSize", (int) FontOption.TABLE_CELL_FONT.getFont().getSize()),
            entry("statementCheckedColor", ColorOption.STATEMENT_CHECKED.getWebString()),
            entry("statementUncheckedColor", ColorOption.STATEMENT_UNCHECKED.getWebString()),
            entry("statementMissingColor", ColorOption.STATEMENT_MISSING.getWebString()),
            // dialogs
            entry("dialogLabelFontFamily", FontOption.DIALOG_LABEL_FONT.getFont().getFamily()),
            entry("dialogLabelFontSize", (int) FontOption.DIALOG_LABEL_FONT.getFont().getSize())
        );

        try (var w = new FileWriter(mainCssFile)) {
            templateEngine().process(TemplateEngine.Template.MAIN_CSS, dataModel, w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        try (var w = new FileWriter(dialogCssFile)) {
            templateEngine().process(TemplateEngine.Template.DIALOG_CSS, dataModel, w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        try (var w = new FileWriter(aboutDialogCssFile)) {
            templateEngine().process(TemplateEngine.Template.ABOUT_DIALOG_CSS, dataModel, w);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void reloadCssFile() {
        WindowManager.newInstance().getControllers().forEach(
            c -> runLater(() -> c.getStage().getScene().getStylesheets().setAll(getMainCssFilePath()))
        );
    }

    public String getMainCssFilePath() {
        try {
            return mainCssFile.toURI().toURL().toExternalForm();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public URL getDialogCssFileUrl() {
        try {
            return dialogCssFile.toURI().toURL();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public URL getAboutDialogCssFileUrl() {
        try {
            return aboutDialogCssFile.toURI().toURL();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private enum Option {
        SHOW_DEACTIVATED_ACCOUNTS,
        MAIN_WINDOW_WIDTH,
        MAIN_WINDOW_HEIGHT,
        AUTO_COMPLETE_LENGTH,
        ACCOUNT_CLOSING_DAY_DELTA,
        LAST_STATEMENT_DIR,
        LAST_EXPORT_DIR,
        FONTS,
        COLORS;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    private static final Preferences PREFS = Preferences.userNodeForPackage(MoneyApplication.class);
    private static final Preferences FONT_PREFS = PREFS.node(Option.FONTS.toString());
    private static final Preferences COLOR_PREFS = PREFS.node(Option.COLORS.toString());

    // Cached values
    private static int autoCompleteLength = PREFS.getInt(Option.AUTO_COMPLETE_LENGTH.toString(), AUTO_COMPLETE_LENGTH);

    public void loadFontOptions() {
        for (var option : FontOption.values()) {
            var prefs = FONT_PREFS.node(option.toString());

            var style = prefs.get("style", DEFAULT_FONT_STYLE);
            var font = Font.font(prefs.get("family", DEFAULT_FONT_FAMILY),
                style.toLowerCase().contains("bold") ? FontWeight.BOLD : FontWeight.NORMAL,
                style.toLowerCase().contains("italic") ? FontPosture.ITALIC : FontPosture.REGULAR,
                prefs.getDouble("size", DEFAULT_FONT_SIZE));

            option.setFont(font);
        }
    }

    public void loadColorOptions() {
        for (var option : ColorOption.values()) {
            var colorString = COLOR_PREFS.get(option.toString(), null);
            if (colorString != null) {
                option.setColor(Color.valueOf(colorString));
            }
        }
    }

    public static boolean getShowDeactivatedAccounts() {
        return PREFS.getBoolean(Option.SHOW_DEACTIVATED_ACCOUNTS.toString(), false);
    }

    public static void setShowDeactivatedAccounts(boolean show) {
        PREFS.putBoolean(Option.SHOW_DEACTIVATED_ACCOUNTS.toString(), show);
    }

    public static double getMainWindowWidth() {
        return PREFS.getDouble(Option.MAIN_WINDOW_WIDTH.toString(), DEFAULT_WIDTH);
    }

    public static void setMainWindowWidth(double x) {
        PREFS.putDouble(Option.MAIN_WINDOW_WIDTH.toString(), x);
    }

    public static double getMainWindowHeight() {
        return PREFS.getDouble(Option.MAIN_WINDOW_HEIGHT.toString(), DEFAULT_HEIGHT);
    }

    public static void setMainWindowHeight(double x) {
        PREFS.putDouble(Option.MAIN_WINDOW_HEIGHT.toString(), x);
    }

    public static int getAutoCompleteLength() {
        return autoCompleteLength;
    }

    static void setAutoCompleteLength(int x) {
        autoCompleteLength = x;
        PREFS.putInt(Option.AUTO_COMPLETE_LENGTH.toString(), x);
    }

    public static void setLastStatementDir(String dir) {
        PREFS.put(Option.LAST_STATEMENT_DIR.toString(), dir);
    }

    public static String getLastStatementDir() {
        return PREFS.get(Option.LAST_STATEMENT_DIR.toString(), "");
    }

    public static Optional<File> getLastExportDir() {
        var dir = PREFS.get(Option.LAST_EXPORT_DIR.toString(), null);
        return dir == null || dir.isEmpty() ? Optional.empty() : Optional.of(new File(dir));
    }

    public static void setLastExportDir(String dir) {
        PREFS.put(Option.LAST_EXPORT_DIR.toString(), dir);
    }

    public static int getAccountClosingDayDelta() {
        return PREFS.getInt(Option.ACCOUNT_CLOSING_DAY_DELTA.toString(), 10);
    }

    public static void setAccountClosingDayDelta(int delta) {
        PREFS.putInt(Option.ACCOUNT_CLOSING_DAY_DELTA.toString(), delta);
    }

    public static void setFont(FontOption option, Font font) {
        if (font == null) {
            return;
        }

        var prefs = FONT_PREFS.node(option.toString());
        prefs.put("family", font.getFamily());
        prefs.put("style", font.getStyle());
        prefs.putDouble("size", font.getSize());

        option.setFont(font);
    }

    public static void setColor(ColorOption option, Color color) {
        if (color == null) {
            return;
        }

        COLOR_PREFS.put(option.toString(), color.toString());
        option.setColor(color);
    }

    public static void saveStageDimensions(Class<?> parentClass, Stage stage) {
        var key = parentClass.getSimpleName().toLowerCase() + "_size";
        var value = String.format("%d;%d;%d;%d",
            (int) stage.getX(),
            (int) stage.getY(),
            (int) stage.getWidth(),
            (int) stage.getHeight());
        PREFS.put(key, value);
    }

    public static void loadStageDimensions(Class<?> parentClass, Stage stage) {
        var key = parentClass.getSimpleName().toLowerCase() + "_size";
        var parts = PREFS.get(key, "").split(";");
        if (parts.length != 4) {
            return;
        }

        stage.setX(Double.parseDouble(parts[0]));
        stage.setY(Double.parseDouble(parts[1]));
        stage.setWidth(Double.parseDouble(parts[2]));
        stage.setHeight(Double.parseDouble(parts[3]));
    }
}
