/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.settings;

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
import static org.panteleyev.money.app.TemplateEngine.templateEngine;

public final class Settings {
    private final ApplicationFiles files;

    private final GeneralSettings generalSettings = new GeneralSettings();
    private final WindowsSettings windowsSettings = new WindowsSettings();
    private final ColorSettings colorSettings = new ColorSettings();
    private final FontSettings fontSettings = new FontSettings();

    public Settings(ApplicationFiles files) {
        this.files = files;
    }

    public void update(Consumer<Settings> block) {
        block.accept(this);
        save();
        generateCssFiles();
        reloadCssFile();
    }

    private void generateCssFiles() {
        var dataModel = Map.ofEntries(
                entry("debitColor", colorSettings.getWebString(ColorName.DEBIT)),
                entry("creditColor", colorSettings.getWebString(ColorName.CREDIT)),
                entry("transferColor", colorSettings.getWebString(ColorName.TRANSFER)),
                entry("controlsFontFamily", fontSettings.getFont(FontName.CONTROLS_FONT).getFamily()),
                entry("controlsFontSize", (int) fontSettings.getFont(FontName.CONTROLS_FONT).getSize()),
                entry("menuFontFamily", fontSettings.getFont(FontName.MENU_FONT).getFamily()),
                entry("menuFontSize", (int) fontSettings.getFont(FontName.MENU_FONT).getSize()),
                entry("tableCellFontFamily", fontSettings.getFont(FontName.TABLE_CELL_FONT).getFamily()),
                entry("tableCellFontSize", (int) fontSettings.getFont(FontName.TABLE_CELL_FONT).getSize()),
                entry("statementCheckedColor", colorSettings.getWebString(ColorName.STATEMENT_CHECKED)),
                entry("statementUncheckedColor", colorSettings.getWebString(ColorName.STATEMENT_UNCHECKED)),
                entry("statementMissingColor", colorSettings.getWebString(ColorName.STATEMENT_MISSING)),
                // dialogs
                entry("dialogLabelFontFamily", fontSettings.getFont(FontName.DIALOG_LABEL_FONT).getFamily()),
                entry("dialogLabelFontSize", (int) fontSettings.getFont(FontName.DIALOG_LABEL_FONT).getSize())
        );

        files.write(ApplicationFiles.AppFile.MAIN_CSS, out -> templateEngine().process(
                TemplateEngine.Template.MAIN_CSS, dataModel, new OutputStreamWriter(out)
        ));

        files.write(ApplicationFiles.AppFile.DIALOG_CSS, out -> templateEngine().process(
                TemplateEngine.Template.DIALOG_CSS, dataModel, new OutputStreamWriter(out)
        ));

        files.write(ApplicationFiles.AppFile.ABOUT_DIALOG_CSS, out -> templateEngine().process(
                TemplateEngine.Template.ABOUT_DIALOG_CSS, dataModel, new OutputStreamWriter(out)
        ));
    }

    private void reloadCssFile() {
        WindowManager.newInstance().getControllers().forEach(
                c -> runLater(() -> c.getStage().getScene().getStylesheets().setAll(getMainCssFilePath()))
        );
    }

    public String getMainCssFilePath() {
        return files.getUrl(ApplicationFiles.AppFile.MAIN_CSS).toExternalForm();
    }

    public URL getDialogCssFileUrl() {
        return files.getUrl(ApplicationFiles.AppFile.DIALOG_CSS);
    }

    public URL getAboutDialogCssFileUrl() {
        return files.getUrl(ApplicationFiles.AppFile.ABOUT_DIALOG_CSS);
    }

    public boolean getShowDeactivatedAccounts() {
        return generalSettings.get(GeneralSettings.Setting.SHOW_DEACTIVATED_ACCOUNTS);
    }

    public void setShowDeactivatedAccounts(boolean show) {
        generalSettings.put(GeneralSettings.Setting.SHOW_DEACTIVATED_ACCOUNTS, show);
    }

    public int getAutoCompleteLength() {
        return generalSettings.get(GeneralSettings.Setting.AUTO_COMPLETE_LENGTH);
    }

    public void setAutoCompleteLength(int x) {
        generalSettings.put(GeneralSettings.Setting.AUTO_COMPLETE_LENGTH, x);
    }

    private Optional<File> getDirectorySetting(GeneralSettings.Setting key) {
        String value = generalSettings.get(key);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        var dir = new File(value);
        return dir.isDirectory() ? Optional.of(dir) : Optional.empty();
    }

    public Optional<File> getLastStatementDir() {
        return getDirectorySetting(GeneralSettings.Setting.LAST_STATEMENT_DIR);
    }

    public void setLastStatementDir(String dir) {
        generalSettings.put(GeneralSettings.Setting.LAST_STATEMENT_DIR, dir == null ? "" : dir);
    }

    public Optional<File> getLastExportDir() {
        return getDirectorySetting(GeneralSettings.Setting.LAST_EXPORT_DIR);
    }

    public void setLastExportDir(String dir) {
        generalSettings.put(GeneralSettings.Setting.LAST_EXPORT_DIR, dir == null ? "" : dir);
    }

    public Optional<File> getLastReportDir() {
        return getDirectorySetting(GeneralSettings.Setting.LAST_REPORT_DIR);
    }

    public void setLastReportDir(String dir) {
        generalSettings.put(GeneralSettings.Setting.LAST_REPORT_DIR, dir == null ? "" : dir);
    }

    public int getAccountClosingDayDelta() {
        return generalSettings.get(GeneralSettings.Setting.ACCOUNT_CLOSING_DAY_DELTA);
    }

    public void setAccountClosingDayDelta(int delta) {
        generalSettings.put(GeneralSettings.Setting.ACCOUNT_CLOSING_DAY_DELTA, delta);
    }

    public Font getFont(FontName option) {
        return fontSettings.getFont(option);
    }

    public void setFont(FontName option, Font font) {
        fontSettings.setFont(option, font);
    }

    public Color getColor(ColorName option) {
        return colorSettings.getColor(option);
    }

    public void setColor(ColorName option, Color color) {
        colorSettings.setColor(option, color);
    }

    public void saveStageDimensions(Controller controller) {
        windowsSettings.storeWindowDimensions(controller);
    }

    public void loadStageDimensions(Controller controller) {
        windowsSettings.restoreWindowDimensions(controller);
    }

    private void save() {
        files.write(ApplicationFiles.AppFile.SETTINGS, generalSettings::save);
        files.write(ApplicationFiles.AppFile.COLORS, colorSettings::save);
        files.write(ApplicationFiles.AppFile.FONTS, fontSettings::save);
    }

    public void saveWindowsSettings() {
        files.write(ApplicationFiles.AppFile.WINDOWS, windowsSettings::save);
    }

    public void load() {
        files.read(ApplicationFiles.AppFile.SETTINGS, generalSettings::load);
        files.read(ApplicationFiles.AppFile.WINDOWS, windowsSettings::load);
        files.read(ApplicationFiles.AppFile.COLORS, colorSettings::load);
        files.read(ApplicationFiles.AppFile.FONTS, fontSettings::load);
        generateCssFiles();
    }
}
