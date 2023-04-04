/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;

import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Constants.FILTER_ZIP_FILES;
import static org.panteleyev.money.app.GlobalContext.settings;

public class ExportFileFialog {
    public Optional<File> showImportDialog(Window owner) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Импорт");

        settings().getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.getExtensionFilters().addAll(FILTER_ZIP_FILES);

        var selected = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(selected);
    }

    public Optional<File> showExportDialog(Window owner) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");

        settings().getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName() + ".zip");
        fileChooser.getExtensionFilters().addAll(FILTER_ZIP_FILES);

        var selected = fileChooser.showSaveDialog(owner);
        return Optional.ofNullable(selected);
    }
}
