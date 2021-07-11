/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
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
import static java.util.Map.entry;
import static javafx.application.Platform.runLater;
import static org.panteleyev.money.app.TemplateEngine.templateEngine;
import static org.panteleyev.money.xml.XMLUtils.appendElement;
import static org.panteleyev.money.xml.XMLUtils.appendTextNode;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
import static org.panteleyev.money.xml.XMLUtils.getAttribute;
import static org.panteleyev.money.xml.XMLUtils.getBooleanNodeValue;
import static org.panteleyev.money.xml.XMLUtils.getIntNodeValue;
import static org.panteleyev.money.xml.XMLUtils.getStringNodeValue;
import static org.panteleyev.money.xml.XMLUtils.readDocument;
import static org.panteleyev.money.xml.XMLUtils.writeDocument;

public final class Options {
    // XML
    private static final String ROOT = "settings";
    private static final String AUTO_COMPLETE_LENGTH_ELEMENT = "autoCompleteLength";
    private static final String ACCOUNT_CLOSING_DAY_DELTA_ELEMENT = "accountClosingDayDelta";
    private static final String SHOW_DEACTIVATED_ACCOUNTS_ELEMENT = "showDeactivatedAccounts";
    private static final String LAST_STATEMENT_DIR_ELEMENT = "lastStatementDir";
    private static final String LAST_EXPORT_DIR_ELEMENT = "lastExportDir";
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

    private static final int DEFAULT_AUTO_COMPLETE_LENGTH = 3;
    private static final int DEFAULT_ACCOUNT_CLOSING_DAY_DELTA = 10;
    private static final String OPTIONS_DIRECTORY = ".money-manager";

    private static final String DEFAULT_FONT_FAMILY = "System";
    private static final String DEFAULT_FONT_STYLE = "Normal Regular";
    private static final double DEFAULT_FONT_SIZE = 12;

    // Settings values
    private int autoCompleteLength = DEFAULT_AUTO_COMPLETE_LENGTH;
    private int accountClosingDayDelta = DEFAULT_ACCOUNT_CLOSING_DAY_DELTA;
    private boolean showDeactivatedAccounts = false;
    private String lastStatementDir = "";
    private String lastExportDir = "";
    // Windows
    private final WindowsSettings windowsSettings = new WindowsSettings();

    private File mainCssFile;
    private File dialogCssFile;
    private File aboutDialogCssFile;

    private File profilesFile;
    private File settingsFile;
    private File windowsFile;

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
        windowsFile = new File(settingsDirectory, "windows.xml");
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

    public boolean getShowDeactivatedAccounts() {
        return showDeactivatedAccounts;
    }

    public void setShowDeactivatedAccounts(boolean show) {
        showDeactivatedAccounts = show;
    }

    public int getAutoCompleteLength() {
        return autoCompleteLength;
    }

    public void setAutoCompleteLength(int x) {
        autoCompleteLength = x;
    }

    public void setLastStatementDir(String dir) {
        lastStatementDir = dir;
    }

    public String getLastStatementDir() {
        return lastStatementDir;
    }

    public Optional<File> getLastExportDir() {
        return lastExportDir == null || lastExportDir.isEmpty() ?
            Optional.empty() : Optional.of(new File(lastExportDir));
    }

    public void setLastExportDir(String dir) {
        lastExportDir = dir;
    }

    public int getAccountClosingDayDelta() {
        return accountClosingDayDelta;
    }

    public void setAccountClosingDayDelta(int delta) {
        accountClosingDayDelta = delta;
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

    public void saveStageDimensions(Controller controller) {
        windowsSettings.storeWindowDimensions(controller);
    }

    public void loadStageDimensions(Controller controller) {
        windowsSettings.restoreWindowDimensions(controller);
    }

    public void saveSettings() {
        try (var out = new FileOutputStream(settingsFile)) {
            var root = createDocument(ROOT);
            appendTextNode(root, AUTO_COMPLETE_LENGTH_ELEMENT, autoCompleteLength);
            appendTextNode(root, ACCOUNT_CLOSING_DAY_DELTA_ELEMENT, accountClosingDayDelta);
            appendTextNode(root, SHOW_DEACTIVATED_ACCOUNTS_ELEMENT, showDeactivatedAccounts);
            appendTextNode(root, LAST_STATEMENT_DIR_ELEMENT, lastStatementDir);
            appendTextNode(root, LAST_EXPORT_DIR_ELEMENT, lastExportDir);
            serializeColors(root);
            serializeFonts(root);
            writeDocument(root.getOwnerDocument(), out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void saveWindowsSettings() {
        windowsSettings.saveWindowsSettings(windowsFile);
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
        windowsSettings.loadWindowsSettings(windowsFile);

        if (!settingsFile.exists()) {
            return;
        }

        try (var in = new FileInputStream(settingsFile)) {
            var rootElement = readDocument(in);
            getIntNodeValue(rootElement, AUTO_COMPLETE_LENGTH_ELEMENT).ifPresent(
                value -> autoCompleteLength = value
            );
            getIntNodeValue(rootElement, ACCOUNT_CLOSING_DAY_DELTA_ELEMENT).ifPresent(
                value -> accountClosingDayDelta = value
            );
            getBooleanNodeValue(rootElement, SHOW_DEACTIVATED_ACCOUNTS_ELEMENT).ifPresent(
                value -> showDeactivatedAccounts = value
            );
            getStringNodeValue(rootElement, LAST_STATEMENT_DIR_ELEMENT).ifPresent(
                value -> lastStatementDir = value
            );
            getStringNodeValue(rootElement, LAST_EXPORT_DIR_ELEMENT).ifPresent(
                value -> lastExportDir = value
            );
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
