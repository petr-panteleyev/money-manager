/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.panteleyev.money.statements.Statement;

import java.io.File;
import java.util.Optional;

import static org.panteleyev.money.app.Constants.OFX_FILES;
import static org.panteleyev.money.app.Constants.SBERBANK_HTML_FILES;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENT;

public class StatementFileDialog {
    private final FileChooser fileChooser = new FileChooser();

    public Optional<File> show(Stage owner) {
        fileChooser.setTitle(UI.getString(I18N_WORD_STATEMENT));

        settings().getLastStatementDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.getExtensionFilters().addAll(OFX_FILES, SBERBANK_HTML_FILES);

        var selected = fileChooser.showOpenDialog(owner);
        return Optional.ofNullable(selected);
    }

    public Statement.StatementType getStatementType() {
        var filter = fileChooser.getSelectedExtensionFilter();
        if (OFX_FILES.equals(filter)) {
            return Statement.StatementType.RAIFFEISEN_OFX;
        } else if (SBERBANK_HTML_FILES.equals(filter)) {
            return Statement.StatementType.SBERBANK_HTML;
        } else {
            return Statement.StatementType.UNKNOWN;
        }
    }
}
