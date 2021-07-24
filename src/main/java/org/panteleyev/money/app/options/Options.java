/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.options;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.app.ApplicationFiles;
import org.panteleyev.money.app.TemplateEngine;
import java.io.File;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import static java.util.Map.entry;
import static javafx.application.Platform.runLater;
import static org.panteleyev.money.app.ApplicationFiles.files;
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

    // Settings values
    private int autoCompleteLength = DEFAULT_AUTO_COMPLETE_LENGTH;
    private int accountClosingDayDelta = DEFAULT_ACCOUNT_CLOSING_DAY_DELTA;
    private boolean showDeactivatedAccounts = false;
    private String lastStatementDir = "";
    private String lastExportDir = "";

    private final WindowsSettings windowsSettings = new WindowsSettings();
    private final ColorSettings colorSettings = new ColorSettings();
    private final FontSettings fontSettings = new FontSettings();

    private static final Options OPTIONS = new Options();

    public static Options options() {
        return OPTIONS;
    }

    private Options() {
    }

    public void update(Consumer<Options> block) {
        block.accept(this);
        save();
        generateCssFiles();
        reloadCssFile();
    }

    private void generateCssFiles() {
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

        files().write(ApplicationFiles.AppFile.MAIN_CSS, out -> templateEngine().process(
            TemplateEngine.Template.MAIN_CSS, dataModel, new OutputStreamWriter(out)
        ));

        files().write(ApplicationFiles.AppFile.DIALOG_CSS, out -> templateEngine().process(
            TemplateEngine.Template.DIALOG_CSS, dataModel, new OutputStreamWriter(out)
        ));

        files().write(ApplicationFiles.AppFile.ABOUT_DIALOG_CSS, out -> templateEngine().process(
            TemplateEngine.Template.ABOUT_DIALOG_CSS, dataModel, new OutputStreamWriter(out)
        ));
    }

    private void reloadCssFile() {
        WindowManager.newInstance().getControllers().forEach(
            c -> runLater(() -> c.getStage().getScene().getStylesheets().setAll(getMainCssFilePath()))
        );
    }

    public String getMainCssFilePath() {
        return files().getUrl(ApplicationFiles.AppFile.MAIN_CSS).toExternalForm();
    }

    public URL getDialogCssFileUrl() {
        return files().getUrl(ApplicationFiles.AppFile.DIALOG_CSS);
    }

    public URL getAboutDialogCssFileUrl() {
        return files().getUrl(ApplicationFiles.AppFile.ABOUT_DIALOG_CSS);
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

    private void save() {
        colorSettings.save();
        fontSettings.save();

        files().write(ApplicationFiles.AppFile.SETTINGS, out -> {
            var root = createDocument(ROOT);
            appendTextNode(root, AUTO_COMPLETE_LENGTH_ELEMENT, autoCompleteLength);
            appendTextNode(root, ACCOUNT_CLOSING_DAY_DELTA_ELEMENT, accountClosingDayDelta);
            appendTextNode(root, SHOW_DEACTIVATED_ACCOUNTS_ELEMENT, showDeactivatedAccounts);
            appendTextNode(root, LAST_STATEMENT_DIR_ELEMENT, lastStatementDir);
            appendTextNode(root, LAST_EXPORT_DIR_ELEMENT, lastExportDir);
            writeDocument(root.getOwnerDocument(), out);
        });
    }

    public void saveWindowsSettings() {
        windowsSettings.save();
    }

    public void load() {
        windowsSettings.load();
        colorSettings.load();
        fontSettings.load();
        generateCssFiles();

        files().read(ApplicationFiles.AppFile.SETTINGS, in -> {
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
        });
    }
}
