/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import org.panteleyev.fx.TableColumnBuilder;
import org.panteleyev.money.app.cells.LocalDateCell;
import org.panteleyev.money.app.cells.StatementRow;
import org.panteleyev.money.app.cells.StatementSumCell;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementParser;
import org.panteleyev.money.statements.StatementPredicate;
import org.panteleyev.money.statements.StatementRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_K;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_O;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_OPEN;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_REPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_UNCHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_EXECUTION_DATE_SHORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_IGNORE_EXECUTION_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_STATEMENT_BALANCE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLEAR;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTRY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_PLACE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class StatementWindowController extends BaseController {
    private final TableView<StatementRecord> statementTable = createStatementTable();
    private final TransactionTableView transactionTable = new TransactionTableView(TransactionTableView.Mode.STATEMENT);
    private final Label ymAccountBalanceLabel = new Label();

    private final ComboBox<Account> accountComboBox = new ComboBox<>();
    private final CheckBox ignoreExecutionDate = newCheckBox(UI, I18N_MISC_IGNORE_EXECUTION_DATE);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Account> accountListener = c -> Platform.runLater(this::setupAccountComboBox);
    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Transaction> transactionListener = c -> calculateTransactions();


    private static final FileChooser.ExtensionFilter OFX_EXTENSION =
        new FileChooser.ExtensionFilter("OFX Statements", "*.ofx");
    private static final FileChooser.ExtensionFilter SBERBANK_HTML =
        new FileChooser.ExtensionFilter("Sberbank HTML Statement", "*.html");

    private Statement.StatementType statementType = Statement.StatementType.UNKNOWN;

    private Statement statement = null;

    StatementWindowController() {
        var root = new BorderPane();

        var balanceBox = hBox(5.0,
            label(fxString(UI, I18N_MISC_STATEMENT_BALANCE, COLON)),
            ymAccountBalanceLabel);
        balanceBox.setAlignment(Pos.CENTER_LEFT);

        var filler1 = new Region();

        var hBox = hBox(5.0,
            label(fxString(UI, I18N_WORD_ACCOUNT, COLON)),
            accountComboBox,
            button(fxString(UI, I18N_WORD_CLEAR), x -> onClear()),
            ignoreExecutionDate,
            filler1,
            balanceBox
        );
        HBox.setHgrow(filler1, Priority.ALWAYS);

        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var splitPane = new SplitPane(statementTable, transactionTable);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPosition(0, 0.9);

        accountComboBox.setConverter(new ReadOnlyNamedConverter<>());

        var centerBox = new BorderPane();
        centerBox.setTop(hBox);
        centerBox.setCenter(splitPane);

        root.setTop(createMainMenu());
        root.setCenter(centerBox);

        transactionTable.setOnCheckTransaction((transactions, check) -> {
            var selected = getSelectedStatementRecord();

            for (Transaction t : transactions) {
                getDao().updateTransaction(t.check(check));
            }

            Platform.runLater(() -> selected.ifPresent(record -> statementTable.getSelectionModel().select(record)));
        });

        cache().getAccounts().addListener(new WeakListChangeListener<>(accountListener));
        cache().getTransactions().addListener(new WeakListChangeListener<>(transactionListener));

        accountComboBox.getSelectionModel()
            .selectedItemProperty().addListener((prop, oldValue, newValue) -> calculateTransactions(newValue));

        setupWindow(root);
        setupAccountComboBox();
        options().loadStageDimensions(this);
    }

    void onNewTransaction(StatementRecord statementRecord) {
        var account = accountComboBox.getSelectionModel().getSelectedItem();
        new TransactionDialog(this, options().getDialogCssFileUrl(), statementRecord, account, cache()).showAndWait()
            .ifPresent(builder -> getDao().insertTransaction(builder));
    }

    void onStatementRecordSelected(StatementRecord statementRecord) {
        transactionTable.setTransactionFilter(
            new StatementPredicate(accountComboBox.getValue(), statementRecord, ignoreExecutionDate.isSelected()));
    }

    @Override
    public String getTitle() {
        return fxString(UI, I18N_WORD_STATEMENTS);
    }

    private Optional<Statement> getStatement() {
        return Optional.ofNullable(statement);
    }

    private MenuBar createMainMenu() {
        return menuBar(
            newMenu(fxString(UI, I18N_MENU_FILE),
                menuItem(fxString(UI, I18N_MENU_ITEM_OPEN, ELLIPSIS), SHORTCUT_O, event -> onBrowse()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_REPORT, ELLIPSIS), event -> onReport()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CLOSE), event -> onClose())),
            newMenu(fxString(UI, I18N_MENU_EDIT),
                menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                    event -> getSelectedStatementRecord().ifPresent(this::onNewTransaction)),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CHECK), SHORTCUT_K,
                    event -> onCheckStatementRecord(true)),
                menuItem(fxString(UI, I18N_MENU_ITEM_UNCHECK), SHORTCUT_U,
                    event -> onCheckStatementRecord(false))
            ),
            createWindowMenu(),
            createHelpMenu());
    }

    private void setupAccountComboBox() {
        var selectionModel = accountComboBox.getSelectionModel();

        var selected = selectionModel.getSelectedItem();
        var selectedUuid = selected == null ? null : selected.uuid();

        var accounts = cache().getAccounts().stream()
            .filter(account -> account.type() == CategoryType.BANKS_AND_CASH
                || account.type() == CategoryType.DEBTS)
            .filter(Account::enabled)
            .sorted((a1, a2) -> a1.name().compareToIgnoreCase(a2.name()))
            .toList();

        accountComboBox.getItems().setAll(accounts);

        accounts.stream().filter(a -> a.uuid().equals(selectedUuid)).findAny().ifPresentOrElse(
            selectionModel::select,
            () -> {
                if (accountComboBox.getItems().isEmpty()) {
                    selectionModel.clearSelection();
                } else {
                    selectionModel.select(0);
                }
            }
        );
    }

    private void setTitle(String title) {
        getStage().setTitle(title);
    }

    private void onBrowse() {
        var chooser = new FileChooser();
        chooser.setTitle(UI.getString(I18N_WORD_STATEMENT));
        chooser.getExtensionFilters().addAll(
            OFX_EXTENSION,
            SBERBANK_HTML
        );

        var lastDirString = options().getLastStatementDir();
        if (!lastDirString.isEmpty()) {
            var lastDir = new File(lastDirString);
            if (lastDir.exists() && lastDir.isDirectory()) {
                chooser.setInitialDirectory(lastDir);
            }
        }

        var selected = chooser.showOpenDialog(null);
        if (selected == null || !selected.exists()) {
            return;
        }

        var filter = chooser.getSelectedExtensionFilter();
        if (OFX_EXTENSION.equals(filter)) {
            statementType = Statement.StatementType.RAIFFEISEN_OFX;
        } else if (SBERBANK_HTML.equals(filter)) {
            statementType = Statement.StatementType.SBERBANK_HTML;
        } else {
            statementType = Statement.StatementType.UNKNOWN;
        }

        var dir = selected.getParentFile();
        options().setLastStatementDir(dir == null ? "" : dir.getAbsolutePath());
        options().saveSettings();

        setTitle(getTitle() + " - " + selected.getAbsolutePath());

        if (statementType.equals(Statement.StatementType.UNKNOWN)) {
            return;
        }

        try (var in = new FileInputStream(selected)) {
            var statement = StatementParser.parse(statementType, in);
            analyzeStatement(statement);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void analyzeStatement(Statement statement) {
        this.statement = statement;
        accountComboBox.getSelectionModel().clearSelection();
        cache().getAccountByNumber(statement.accountNumber())
            .ifPresentOrElse(a -> accountComboBox.getSelectionModel().select(a),
                () -> accountComboBox.getSelectionModel().selectFirst());
        ymAccountBalanceLabel.setText(statement.balance().toString());
    }

    private void onClear() {
        setTitle(getTitle());
        statementType = Statement.StatementType.UNKNOWN;
        statementTable.getItems().clear();
    }

    private void calculateTransactions() {
        calculateTransactions(accountComboBox.getSelectionModel().getSelectedItem());
    }

    private void calculateTransactions(Account account) {
        if (statement == null || account == null) {
            return;
        }

        for (var record : statement.records()) {
            record.setTransactions(cache().getTransactions().stream()
                .filter(new StatementPredicate(account, record, ignoreExecutionDate.isSelected()))
                .toList());
        }

        Platform.runLater(() -> {
            var selected = statementTable.getSelectionModel().getSelectedItem();
            statementTable.getItems().setAll(statement.records());
            if (statement.records().contains(selected)) {
                statementTable.getSelectionModel().select(selected);
            }
        });
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(fxString(UI, I18N_WORD_REPORT));
        options().getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
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

    private void onCheckStatementRecord(boolean check) {
        if (statementTable.isFocused()) {
            getSelectedStatementRecord().ifPresent(record -> onCheckStatementRecord(record, check));
        } else if (transactionTable.checkFocus()) {
            transactionTable.onCheckTransactions(check);
        }
    }

    void onCheckStatementRecord(StatementRecord record, boolean check) {
        var transactions = new ArrayList<>(record.getTransactions());
        for (var t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
        Platform.runLater(() -> statementTable.getSelectionModel().select(record));
    }

    private TableView<StatementRecord> createStatementTable() {
        var tableView = new TableView<StatementRecord>();

        tableView.setRowFactory(x -> new StatementRow());

        var w = tableView.widthProperty().subtract(20);
        tableView.getColumns().addAll(List.of(
            tableColumn(fxString(UI, I18N_WORD_DATE), (TableColumnBuilder<StatementRecord, LocalDate> b) ->
                b.withCellFactory(x -> new LocalDateCell<>())
                    .withPropertyCallback(StatementRecord::getActual)
                    .withWidthBinding(w.multiply(0.05))),
            tableColumn(fxString(UI, I18N_MISC_EXECUTION_DATE_SHORT), (TableColumnBuilder<StatementRecord, LocalDate> b) ->
                b.withCellFactory(x -> new LocalDateCell<>())
                    .withPropertyCallback(StatementRecord::getExecution)
                    .withWidthBinding(w.multiply(0.05))),
            tableColumn(fxString(UI, I18N_WORD_DESCRIPTION), b ->
                b.withPropertyCallback(StatementRecord::getDescription).withWidthBinding(w.multiply(0.5))),
            tableColumn(fxString(UI, I18N_WORD_COUNTERPARTY), b ->
                b.withPropertyCallback(StatementRecord::getCounterParty).withWidthBinding(w.multiply(0.15))),
            tableColumn(fxString(UI, I18N_WORD_PLACE), b ->
                b.withPropertyCallback(StatementRecord::getPlace).withWidthBinding(w.multiply(0.10))),
            tableColumn(fxString(UI, I18N_WORD_COUNTRY), b ->
                b.withPropertyCallback(StatementRecord::getCountry).withWidthBinding(w.multiply(0.05))),
            tableObjectColumn(fxString(UI, I18N_WORD_SUM), b ->
                b.withCellFactory(x -> new StatementSumCell()).withWidthBinding(w.multiply(0.1)))
        ));

        var menu = new ContextMenu();
        menu.getItems().addAll(menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS),
            event -> getSelectedStatementRecord().ifPresent(this::onNewTransaction)));
        tableView.setContextMenu(menu);

        tableView.getSelectionModel().selectedItemProperty().addListener((x, y, newValue) ->
            onStatementRecordSelected(newValue));

        return tableView;
    }

    private Optional<StatementRecord> getSelectedStatementRecord() {
        return Optional.ofNullable(statementTable.getSelectionModel().getSelectedItem());
    }
}
