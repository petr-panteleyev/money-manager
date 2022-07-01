/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

import static org.panteleyev.money.app.Constants.FILTER_STATEMENT_FILES;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENT;

public class StatementFileDialog {
    private final FileChooser fileChooser = new FileChooser();

    public Optional<File> show(Stage owner) {
        fileChooser.setTitle(UI.getString(I18N_WORD_STATEMENT));

        settings().getLastStatementDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.getExtensionFilters().add(FILTER_STATEMENT_FILES);

        var selected = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(selected);
    }
}
