/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.panteleyev.money.app.ReportType;

import java.io.File;
import java.util.Optional;

import static org.panteleyev.money.app.Constants.FILTER_HTML_FILES;
import static org.panteleyev.money.app.GlobalContext.settings;

public class ReportFileDialog {
    public Optional<File> show(Stage owner, ReportType reportType) {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Отчет");

        settings().getLastReportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(reportType.generateReportName() + ".html");
        fileChooser.getExtensionFilters().add(FILTER_HTML_FILES);

        var selected = fileChooser.showSaveDialog(owner);
        return Optional.ofNullable(selected);
    }
}
