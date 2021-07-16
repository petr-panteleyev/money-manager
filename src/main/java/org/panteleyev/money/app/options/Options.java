/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.app.TemplateEngine;
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
import static org.panteleyev.money.xml.XMLUtils.appendTextNode;
import static org.panteleyev.money.xml.XMLUtils.createDocument;
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

    private static final int DEFAULT_AUTO_COMPLETE_LENGTH = 3;
    private static final int DEFAULT_ACCOUNT_CLOSING_DAY_DELTA = 10;
    private static final String OPTIONS_DIRECTORY = ".money-manager";

    // Settings values
    private int autoCompleteLength = DEFAULT_AUTO_COMPLETE_LENGTH;
    private int accountClosingDayDelta = DEFAULT_ACCOUNT_CLOSING_DAY_DELTA;
    private boolean showDeactivatedAccounts = false;
    private String lastStatementDir = "";
    private String lastExportDir = "";

    private final WindowsSettings windowsSettings = new WindowsSettings();
    private final ColorSettings colorSettings = new ColorSettings();
    private final FontSettings fontSettings = new FontSettings();

    private File mainCssFile;
    private File dialogCssFile;
    private File aboutDialogCssFile;

    private File profilesFile;
    private File settingsFile;
    private File windowsFile;
    private File colorsFile;
    private File fontsFile;

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
        colorsFile = new File(settingsDirectory, "colors.xml");
        fontsFile = new File(settingsDirectory, "fonts.xml");
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
            entry("debitColor", colorSettings.getWebString(ColorOption.DEBIT)),
            entry("creditColor", colorSettings.getWebString(ColorOption.CREDIT)),
            entry("transferColor", colorSettings.getWebString(ColorOption.TRANSFER)),
            entry("controlsFontFamily", fontSettings.getFont(FontOption.CONTROLS_FONT).getFamily()),
            entry("controlsFontSize", (int) fontSettings.getFont(FontOption.CONTROLS_FONT).getSize()),
            entry("menuFontFamily", fontSettings.getFont(FontOption.MENU_FONT).getFamily()),
            entry("menuFontSize", (int) fontSettings.getFont(FontOption.MENU_FONT).getSize()),
            entry("tableCellFontFamily", fontSettings.getFont(FontOption.TABLE_CELL_FONT).getFamily()),
            entry("tableCellFontSize", (int) fontSettings.getFont(FontOption.TABLE_CELL_FONT).getSize()),
            entry("statementCheckedColor", colorSettings.getWebString(ColorOption.STATEMENT_CHECKED)),
            entry("statementUncheckedColor", colorSettings.getWebString(ColorOption.STATEMENT_UNCHECKED)),
            entry("statementMissingColor", colorSettings.getWebString(ColorOption.STATEMENT_MISSING)),
            // dialogs
            entry("dialogLabelFontFamily", fontSettings.getFont(FontOption.DIALOG_LABEL_FONT).getFamily()),
            entry("dialogLabelFontSize", (int) fontSettings.getFont(FontOption.DIALOG_LABEL_FONT).getSize())
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

    public Font getFont(FontOption option) {
        return fontSettings.getFont(option);
    }

    public void setFont(FontOption option, Font font) {
        fontSettings.setFont(option, font);
    }

    public Color getColor(ColorOption option) {
        return colorSettings.getColor(option);
    }

    public void setColor(ColorOption option, Color color) {
        colorSettings.setColor(option, color);
    }

    public void saveStageDimensions(Controller controller) {
        windowsSettings.storeWindowDimensions(controller);
    }

    public void loadStageDimensions(Controller controller) {
        windowsSettings.restoreWindowDimensions(controller);
    }

    public void saveSettings() {
        colorSettings.save(colorsFile);
        fontSettings.save(fontsFile);

        try (var out = new FileOutputStream(settingsFile)) {
            var root = createDocument(ROOT);
            appendTextNode(root, AUTO_COMPLETE_LENGTH_ELEMENT, autoCompleteLength);
            appendTextNode(root, ACCOUNT_CLOSING_DAY_DELTA_ELEMENT, accountClosingDayDelta);
            appendTextNode(root, SHOW_DEACTIVATED_ACCOUNTS_ELEMENT, showDeactivatedAccounts);
            appendTextNode(root, LAST_STATEMENT_DIR_ELEMENT, lastStatementDir);
            appendTextNode(root, LAST_EXPORT_DIR_ELEMENT, lastExportDir);
            writeDocument(root.getOwnerDocument(), out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void saveWindowsSettings() {
        windowsSettings.save(windowsFile);
    }

    public void loadSettings() {
        windowsSettings.load(windowsFile);
        colorSettings.load(colorsFile);
        fontSettings.load(fontsFile);

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
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
