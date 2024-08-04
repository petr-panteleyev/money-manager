/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

import static org.panteleyev.money.app.Constants.FILTER_STATEMENT_FILES;
import static org.panteleyev.money.app.GlobalContext.settings;

public class StatementFileDialog {
    private final FileChooser fileChooser = new FileChooser();

    public Optional<File> show(Stage owner) {
        fileChooser.setTitle("Выписка");

        settings().getLastStatementDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.getExtensionFilters().add(FILTER_STATEMENT_FILES);

        var selected = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(selected);
    }
}
