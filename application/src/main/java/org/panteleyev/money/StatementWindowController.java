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

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.WeakMapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementParser;
import org.panteleyev.money.statements.StatementPredicate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.commons.fx.FXFactory.newButton;
import static org.panteleyev.commons.fx.FXFactory.newCheckBox;
import static org.panteleyev.commons.fx.FXFactory.newLabel;
import static org.panteleyev.commons.fx.FXFactory.newMenu;
import static org.panteleyev.commons.fx.FXFactory.newMenuBar;
import static org.panteleyev.commons.fx.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class StatementWindowController extends BaseController {
    private static final ResourceBundle SOURCE_TYPE_RB =
        ResourceBundle.getBundle("org.panteleyev.money.res.SourceType");

    private enum SourceType {
        FILE,
        YANDEX_MONEY;

        public String toString() {
            return SOURCE_TYPE_RB.getString(name());
        }
    }

    private final StatementView statementTable = new StatementView();
    private final TransactionTableView transactionTable = new TransactionTableView(TransactionTableView.Mode.STATEMENT);
    private final Label ymAccountBalanceLabel = new Label();

    private final ComboBox<Account> accountComboBox = new ComboBox<>();
    private final TextField statementFileEdit = new TextField();
    private final CheckBox ignoreExecutionDate = newCheckBox(RB, "check.IgnoreExecutionDate");

    private final YandexMoneyClient yandexMoneyClient = new YandexMoneyClient(Options.getYandexMoneyToken());

    private final DatePicker ymFromPicker = new DatePicker(LocalDate.now().minusMonths(3));
    private final DatePicker ymToPicker = new DatePicker(LocalDate.now());
    private final ComboBox<Integer> limitComboBox =
        new ComboBox<>(FXCollections.observableArrayList(100, 75, 50, 25));

    private final ComboBox<SourceType> sourceTypeComboBox =
        new ComboBox<>(FXCollections.observableArrayList(SourceType.values()));

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Account> accountListener =
        change -> Platform.runLater(this::setupAccountComboBox);
    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Transaction> transactionListener =
        change -> calculateTransactions();


    private static final FileChooser.ExtensionFilter OFX_EXTENSION =
        new FileChooser.ExtensionFilter("OFX Statements", "*.ofx");
    private static final FileChooser.ExtensionFilter SBERBANK_HTML =
        new FileChooser.ExtensionFilter("Sberbank HTML Statement", "*.html");
    private static final FileChooser.ExtensionFilter YANDEX_CSV =
        new FileChooser.ExtensionFilter("Yandex Money Statement", "*.csv");
    private static final FileChooser.ExtensionFilter ALFA_CSV =
        new FileChooser.ExtensionFilter("AlfaBank Statement", "*.csv");

    private Statement.StatementType statementType = Statement.StatementType.UNKNOWN;

    private Statement statement = null;

    StatementWindowController() {
        var root = new BorderPane();

        sourceTypeComboBox.getSelectionModel().select(0);

        // File load controls
        var fileLoadControls = new HBox(5.0, statementFileEdit,
            newButton("...", x -> onBrowse()));
        fileLoadControls.visibleProperty().bind(
            sourceTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(SourceType.FILE));
        fileLoadControls.setAlignment(Pos.CENTER_LEFT);
        ////////////////////////////////////////////////////////

        // Yandex Money controls
        var ymAuthButton = newButton("Authorize...", x -> yandexMoneyClient.authorize());

        limitComboBox.getSelectionModel().selectFirst();
        var yandexMoneyControls = new HBox(5.0,
            ymAuthButton,
            ymFromPicker,
            new Label(" - "),
            ymToPicker,
            limitComboBox);
        yandexMoneyControls.setAlignment(Pos.CENTER_LEFT);

        yandexMoneyControls.visibleProperty().bind(
            sourceTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(SourceType.YANDEX_MONEY));
        ////////////////////////////////////////////////////////

        var stackPane = new StackPane(fileLoadControls, yandexMoneyControls);

        var balanceBox = new HBox(5.0,
            newLabel(RB, "label.StatementBalance"),
            ymAccountBalanceLabel);
        balanceBox.setAlignment(Pos.CENTER_LEFT);

        var filler1 = new Region();

        var hBox = new HBox(5.0,
            newLabel(RB, "label.Account"),
            accountComboBox,
            newButton(RB, "button.Load", x -> onLoad()),
            newButton(RB, "button.Clear", x -> onClear()),
            ignoreExecutionDate,
            filler1,
            balanceBox
        );
        HBox.setHgrow(filler1, Priority.ALWAYS);

        statementFileEdit.setPrefColumnCount(40);
        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var splitPane = new SplitPane(statementTable, transactionTable);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.9);

        transactionTable.setFocusTraversable(false);

        accountComboBox.setConverter(new ReadOnlyNamedConverter<>());

        var lowerBox = new HBox(5.0, sourceTypeComboBox, stackPane);

        var toolBar = new VBox(5.0, lowerBox, hBox);
        BorderPane.setMargin(toolBar, new Insets(5.0, 5.0, 5.0, 5.0));

        var centerBox = new BorderPane();
        centerBox.setTop(toolBar);
        centerBox.setCenter(splitPane);

        root.setTop(createMainMenu());
        root.setCenter(centerBox);

        statementTable.setRecordSelectedCallback(record ->
            transactionTable.setTransactionFilter(new StatementPredicate(accountComboBox.getValue(), record,
                ignoreExecutionDate.isSelected())));

        transactionTable.setOnCheckTransaction((transactions, check) -> {
            var selected = statementTable.getSelectedRecord();

            for (Transaction t : transactions) {
                getDao().updateTransaction(t.check(check));
            }

            Platform.runLater(() -> selected.ifPresent(statementTable::setSelectedRecord));
        });

        cache().accounts().addListener(new WeakMapChangeListener<>(accountListener));
        cache().transactions().addListener(new WeakMapChangeListener<>(transactionListener));

        accountComboBox.getSelectionModel()
            .selectedItemProperty().addListener((prop, oldValue, newValue) -> calculateTransactions(newValue));

        statementTable.setNewTransactionCallback(record -> {
            var account = accountComboBox.getSelectionModel().getSelectedItem();
            var mainWindow = getController(MainWindowController.class);
            mainWindow.handleStatementRecord(record, account);
        });

        setupWindow(root);
        setupAccountComboBox();
        Options.loadStageDimensions(getClass(), getStage());
    }

    @Override
    public String getTitle() {
        return RB.getString("statement.window.title");
    }

    private Optional<Statement> getStatement() {
        return Optional.ofNullable(statement);
    }

    private MenuBar createMainMenu() {
        return newMenuBar(
            newMenu(RB, "menu.File",
                newMenuItem(RB, "menu.File.Report", event -> onReport()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.File.Close", event -> onClose())),
            createWindowMenu(RB),
            createHelpMenu(RB));
    }

    private void setupAccountComboBox() {
        var accounts = cache().getAccounts().stream()
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
        var chooser = new FileChooser();
        chooser.setTitle(RB.getString("Statement"));
        chooser.getExtensionFilters().addAll(
            OFX_EXTENSION,
            SBERBANK_HTML,
            YANDEX_CSV,
            ALFA_CSV
        );

        var lastDirString = Options.getLastStatementDir();
        if (!lastDirString.isEmpty()) {
            var lastDir = new File(lastDirString);
            if (lastDir.exists() && lastDir.isDirectory()) {
                chooser.setInitialDirectory(lastDir);
            }
        }

        var selected = chooser.showOpenDialog(null);
        if (selected != null) {
            var filter = chooser.getSelectedExtensionFilter();
            if (OFX_EXTENSION.equals(filter)) {
                statementType = Statement.StatementType.RAIFFEISEN_OFX;
            } else if (SBERBANK_HTML.equals(filter)) {
                statementType = Statement.StatementType.SBERBANK_HTML;
            } else if (YANDEX_CSV.equals(filter)) {
                statementType = Statement.StatementType.YANDEX_MONEY_CSV;
            } else if (ALFA_CSV.equals(filter)) {
                statementType = Statement.StatementType.ALFA_BANK_CSV;
            } else {
                statementType = Statement.StatementType.UNKNOWN;
            }

            var dir = selected.getParentFile();
            Options.setLastStatementDir(dir == null ? "" : dir.getAbsolutePath());
        }

        statementFileEdit.setText(selected != null ? selected.getAbsolutePath() : "");
    }

    private void onLoad() {
        switch (getSourceType()) {
            case FILE:
                if (statementType.equals(Statement.StatementType.UNKNOWN)) {
                    return;
                }

                var file = new File(statementFileEdit.getText());
                if (!file.exists()) {
                    return;
                }

                try (var in = new FileInputStream(file)) {
                    var statement = StatementParser.parse(statementType, in);
                    analyzeStatement(statement);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }

                break;

            case YANDEX_MONEY:
                var statement = yandexMoneyClient.load(limitComboBox.getSelectionModel().getSelectedItem(),
                    ymFromPicker.getValue(), ymToPicker.getValue());
                analyzeStatement(statement);
                break;
        }
    }

    private void analyzeStatement(Statement statement) {
        this.statement = statement;
        accountComboBox.getSelectionModel().clearSelection();
        cache().getAccountByNumber(statement.getAccountNumber())
            .ifPresentOrElse(a -> accountComboBox.getSelectionModel().select(a),
                () -> accountComboBox.getSelectionModel().selectFirst());
        ymAccountBalanceLabel.setText(statement.getBalance().toString());
    }

    private void onClear() {
        statementFileEdit.setText("");
        statementType = Statement.StatementType.UNKNOWN;
        statementTable.clear();
    }

    private SourceType getSourceType() {
        return sourceTypeComboBox.getSelectionModel().getSelectedItem();
    }

    private void calculateTransactions() {
        calculateTransactions(accountComboBox.getSelectionModel().getSelectedItem());
    }

    private void calculateTransactions(Account account) {
        if (statement == null || account == null) {
            return;
        }

        for (var record : statement.getRecords()) {
            record.setTransactions(cache().getTransactions().stream()
                .filter(new StatementPredicate(account, record, ignoreExecutionDate.isSelected()))
                .collect(Collectors.toList()));
        }

        Platform.runLater(() -> statementTable.setStatement(statement));
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Report");
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("statement"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            getStatement().ifPresent(statement -> Reports.reportStatement(statement, outputStream));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
