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
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import static org.panteleyev.money.xml.XMLUtils.appendElement;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.getAttribute;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

public final class Options {
    // XML
    private static final String ROOT = "settings";
    // Colors
    private static final String COLOR_ELEMENT = "color";
    private static final String COLOR_ATTR_NAME = "name";
    private static final String COLOR_ATTR_VALUE = "value";
    // Fonts
    private static final String FONT_ELEMENT = "font";
    private static final String FONT_ATTR_NAME = "name";
    private static final String FONT_ATTR_FAMILY = "family";
    private static final String FONT_ATTR_STYLE = "style";
    private static final String FONT_ATTR_SIZE = "size";

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

    private File profilesFile;
    private File settingsFile;

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
        profilesFile = new File(settingsDirectory, "profiles.xml");
        settingsFile = new File(settingsDirectory, "settings.xml");
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

    public File getProfilesFile() {
        return profilesFile;
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

    // Cached values
    private static int autoCompleteLength = PREFS.getInt(Option.AUTO_COMPLETE_LENGTH.toString(), AUTO_COMPLETE_LENGTH);

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

        option.setFont(font);
    }

    public static void setColor(ColorOption option, Color color) {
        if (color == null) {
            return;
        }

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

    public void saveSettings() {
        try (var out = new FileOutputStream(settingsFile)) {
            var root = createDocument(ROOT);
            serializeColors(root);
            serializeFonts(root);
            writeDocument(root.getOwnerDocument(), out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void serializeColors(Element parent) {
        var colorRoot = appendElement(parent, "colors");
        for (var opt : ColorOption.values()) {
            var e = appendElement(colorRoot, COLOR_ELEMENT);
            e.setAttribute(COLOR_ATTR_NAME, opt.toString());
            e.setAttribute(COLOR_ATTR_VALUE, opt.getWebString());
        }
    }

    private void serializeFonts(Element parent) {
        var fontRoot = appendElement(parent, "fonts");
        for (var opt: FontOption.values()) {
            var e = appendElement(fontRoot, FONT_ELEMENT);
            var font = opt.getFont();
            e.setAttribute(FONT_ATTR_NAME, opt.toString());
            e.setAttribute(FONT_ATTR_FAMILY, font.getFamily());
            e.setAttribute(FONT_ATTR_STYLE, font.getStyle());
            e.setAttribute(FONT_ATTR_SIZE, Double.toString(font.getSize()));
        }
    }

    public void loadSettings() {
        if (!settingsFile.exists()) {
            return;
        }

        try (var in = new FileInputStream(settingsFile)) {
            var rootElement = readDocument(in);

            deserializeColors(rootElement);
            deserializeFonts(rootElement);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void deserializeColors(Element root) {
        var colorNodes = root.getElementsByTagName(COLOR_ELEMENT);
        for (int i = 0; i < colorNodes.getLength(); i++) {
            var colorElement = (Element)colorNodes.item(i);
            ColorOption.of(colorElement.getAttribute(COLOR_ATTR_NAME).toUpperCase())
                .ifPresent(option -> option.setColor(Color.valueOf(colorElement.getAttribute(COLOR_ATTR_VALUE))));
        }
    }

    private void deserializeFonts(Element root) {
        var fontNodes = root.getElementsByTagName(FONT_ELEMENT);
        for (int i = 0; i < fontNodes.getLength(); i++) {
            var fontElement = (Element)fontNodes.item(i);
            FontOption.of(fontElement.getAttribute(FONT_ATTR_NAME).toUpperCase()).ifPresent(option -> {
                var family = getAttribute(fontElement, FONT_ATTR_FAMILY, DEFAULT_FONT_FAMILY);
                var style = getAttribute(fontElement, FONT_ATTR_STYLE, DEFAULT_FONT_STYLE);
                var size = getAttribute(fontElement, FONT_ATTR_SIZE, DEFAULT_FONT_SIZE);

                var font = Font.font(family,
                    style.toLowerCase().contains("bold") ? FontWeight.BOLD : FontWeight.NORMAL,
                    style.toLowerCase().contains("italic") ? FontPosture.ITALIC : FontPosture.REGULAR,
                    size);

                option.setFont(font);
            });
        }
    }
}
