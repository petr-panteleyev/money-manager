/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.statements;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.panteleyev.money.TransactionTableView;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.persistence.Transaction;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class StatementTab extends BorderPane {
    private final StatementView statementTable = new StatementView();
    private final TransactionTableView transactionTable = new TransactionTableView(true);

    private final ComboBox<Account> accountComboBox = new ComboBox<>();
    private final TextField statementFileEdit = new TextField();
    private final CheckBox ignoreExecutionDate = new CheckBox(RB.getString("check.IgnoreExecutionDate"));

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<Integer, Account> accountListener = change ->
            Platform.runLater(this::setupAccountComboBox);

    private static final FileChooser.ExtensionFilter OFX_EXTENSION =
            new FileChooser.ExtensionFilter("OFX Statements", "*.ofx");
    private static final FileChooser.ExtensionFilter RBA_STATEMENT_CSV =
            new FileChooser.ExtensionFilter("Raiffeisen Statement", "*.csv");
    private static final FileChooser.ExtensionFilter SBERBANK_HTML =
            new FileChooser.ExtensionFilter("Sberbank HTML Statement", "*.html");

    private Statement.StatementType statementType = Statement.StatementType.UNKNOWN;

    private BiConsumer<StatementRecord, Account> newTransactionCallback = (x, y) -> {
    };

    public StatementTab() {
        Button browseButton = new Button("...");
        browseButton.setOnAction(event -> onBrowse());

        Button loadButton = new Button(RB.getString("button.Load"));
        loadButton.setOnAction(event -> onLoad());
        loadButton.disableProperty().bind(statementFileEdit.textProperty().isEmpty());

        Button clearButton = new Button(RB.getString("button.Clear"));
        clearButton.setOnAction(event -> onClear());

        HBox hBox = new HBox(5.0,
                new Label(RB.getString("label.Account")),
                accountComboBox,
                statementFileEdit,
                browseButton,
                loadButton,
                clearButton,
                ignoreExecutionDate
        );

        statementFileEdit.setPrefColumnCount(40);
        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        SplitPane splitPane = new SplitPane(statementTable, transactionTable);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.9);

        accountComboBox.setConverter(new ReadOnlyNamedConverter<>());

        setTop(hBox);
        setCenter(splitPane);

        statementTable.setRecordSelectedCallback(record ->
                transactionTable.setTransactionFilter(new StatementPredicate(accountComboBox.getValue(), record,
                        ignoreExecutionDate.isSelected())));

        transactionTable.setOnCheckTransaction((transactions, check) -> {
            for (Transaction t : transactions) {
                getDao().updateTransaction(t.check(check));
            }
        });

        getDao().accounts().addListener(accountListener);
        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::setupAccountComboBox);
            }
        });

        statementTable.setNewTransactionCallback(record -> {
            Account account = accountComboBox.getSelectionModel().getSelectedItem();
            newTransactionCallback.accept(record, account);
        });
    }

    private void setupAccountComboBox() {
        List<Account> accounts = getDao().getAccounts().stream()
                .filter(account -> account.getType() == CategoryType.BANKS_AND_CASH || account.getType() ==
                        CategoryType.DEBTS)
                .filter(Account::getEnabled)
                .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
                .collect(Collectors.toList());

        accountComboBox.getItems().clear();
        accountComboBox.getItems().addAll(accounts);
        if (!accountComboBox.getItems().isEmpty()) {
            accountComboBox.getSelectionModel().select(0);
        } else {
            accountComboBox.getSelectionModel().select(null);
        }
    }

    private void onBrowse() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(RB.getString("Statement"));
        chooser.getExtensionFilters().addAll(
                RBA_STATEMENT_CSV,
                OFX_EXTENSION,
                SBERBANK_HTML
        );

        File selected = chooser.showOpenDialog(null);
        if (selected != null) {
            FileChooser.ExtensionFilter filter = chooser.getSelectedExtensionFilter();
            if (OFX_EXTENSION.equals(filter)) {
                statementType = Statement.StatementType.RAIFFEISEN_CARD_OFX;
            } else if (RBA_STATEMENT_CSV.equals(filter)) {
                statementType = Statement.StatementType.RAIFFEISEN_CREDIT_CARD_CSV;
            } else if (SBERBANK_HTML.equals(filter)) {
                statementType = Statement.StatementType.SBERBANK_HTML;
            } else {
                statementType = Statement.StatementType.UNKNOWN;
            }
        }

        statementFileEdit.setText(selected != null ? selected.getAbsolutePath() : "");
    }

    private void onLoad() {
        if (statementType.equals(Statement.StatementType.UNKNOWN)) {
            return;
        }

        File file = new File(statementFileEdit.getText());
        if (!file.exists()) {
            return;
        }

        try (InputStream in = new FileInputStream(file)) {
            Statement statement = StatementParser.parse(statementType, in);
            analyzeStatement(statement);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void analyzeStatement(Statement statement) {
        statementTable.setStatement(statement);
    }

    private void onClear() {
        statementFileEdit.setText("");
        statementType = Statement.StatementType.UNKNOWN;
        statementTable.clear();
    }

    public void setNewTransactionCallback(BiConsumer<StatementRecord, Account> callback) {
        Objects.requireNonNull(callback);
        newTransactionCallback = callback;
    }
}
