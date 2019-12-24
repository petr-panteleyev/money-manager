/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import javafx.geometry.Insets;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.MoneyDAO;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.commons.fx.FXFactory.newButton;
import static org.panteleyev.commons.fx.FXFactory.newMenu;
import static org.panteleyev.commons.fx.FXFactory.newMenuBar;
import static org.panteleyev.commons.fx.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class RequestWindowController extends BaseController {
    private final TransactionTableView table = new TransactionTableView(TransactionTableView.Mode.QUERY);

    private final AccountFilterSelectionBox accBox = new AccountFilterSelectionBox();
    private final TransactionFilterBox transactionFilterBox = new TransactionFilterBox(true, true);

    RequestWindowController() {
        var vBox = new VBox(5.0,
            new HBox(5.0,
                newButton(RB, "button.Clear", x -> onClearButton()),
                newButton(RB, "button.Find", x -> onFindButton())),
            new HBox(5.0, accBox, transactionFilterBox));

        var centerBox = new BorderPane();
        centerBox.setTop(vBox);
        centerBox.setCenter(table);

        BorderPane.setMargin(vBox, new Insets(5.0, 5.0, 5.0, 5.0));

        table.setOnCheckTransaction(this::onCheckTransaction);

        var root = new BorderPane();
        root.setTop(createMenuBar());
        root.setCenter(centerBox);

        accBox.setupCategoryTypesBox();
        transactionFilterBox.setFilterYears();

        setupWindow(root);
        Options.loadStageDimensions(getClass(), getStage());
    }

    @Override
    public String getTitle() {
        return RB.getString("tab.Requests");
    }

    private Predicate<Transaction> getTransactionFilter() {
        return table.getTransactionFilter();
    }

    private MenuBar createMenuBar() {
        return newMenuBar(
            newMenu(RB, "menu.File",
                newMenuItem(RB, "menu.File.Report", event -> onReport()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.File.Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "menu.item.details", x -> table.onTransactionDetails()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.item.check",
                    new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN),
                    x -> table.onCheckTransactions(true)),
                newMenuItem(RB, "menu.item.uncheck",
                    new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN),
                    x -> table.onCheckTransactions(false))
            ),
            createWindowMenu(RB),
            createHelpMenu(RB));
    }

    private void onFindButton() {
        table.setTransactionFilter(accBox.getTransactionFilter()
            .and(transactionFilterBox.getTransactionFilter()));
    }

    private void onClearButton() {
        table.setTransactionFilter(x -> false);
        accBox.setupCategoryTypesBox();
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (Transaction t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
    }

    void showTransactionsForAccount(Account account) {
        accBox.setAccount(account);
        onFindButton();
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Report");
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("transactions"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            var transactions = cache().getTransactions(getTransactionFilter())
                .sorted(MoneyDAO.COMPARE_TRANSACTION_BY_DATE)
                .collect(Collectors.toList());
            Reports.reportTransactions(transactions, outputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
