/*
 Copyright © 2017-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
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
import org.panteleyev.fx.TableColumnBuilder;
import org.panteleyev.money.app.cells.LocalDateCell;
import org.panteleyev.money.app.cells.StatementRow;
import org.panteleyev.money.app.cells.StatementSumCell;
import org.panteleyev.money.app.dialogs.ReportFileDialog;
import org.panteleyev.money.app.dialogs.StatementFileDialog;
import org.panteleyev.money.app.transaction.TransactionDialog;
import org.panteleyev.money.app.transaction.TransactionTableView;
import org.panteleyev.money.desktop.commons.ReadOnlyNamedConverter;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.statements.RawStatementData;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementParser;
import org.panteleyev.money.statements.StatementPredicate;
import org.panteleyev.money.statements.StatementRecord;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_K;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_O;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.Styles.BIG_INSETS;

public class StatementWindowController extends BaseController {
    private final TableView<StatementRecord> statementTable = createStatementTable();
    private final TransactionTableView transactionTable
            = new TransactionTableView(this, TransactionTableView.Mode.STATEMENT);
    private final Label ymAccountBalanceLabel = new Label();

    private final ComboBox<Account> accountComboBox = new ComboBox<>();
    private final CheckBox ignoreExecutionDate = new CheckBox("Игнорировать дату исполнения");

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Account> accountListener = _ -> Platform.runLater(this::setupAccountComboBox);
    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Transaction> transactionListener = _ -> calculateTransactions();

    private Statement statement = null;

    StatementWindowController() {
        var root = new BorderPane();
        ignoreExecutionDate.setSelected(true);

        var balanceBox = hBox(5.0,
                label("Баланс по выписке:"),
                ymAccountBalanceLabel);
        balanceBox.setAlignment(Pos.CENTER_LEFT);

        var filler1 = new Region();

        var hBox = hBox(5.0,
                label("Счёт:"),
                accountComboBox,
                button("Очистить", _ -> onClear()),
                ignoreExecutionDate,
                filler1,
                balanceBox
        );
        HBox.setHgrow(filler1, Priority.ALWAYS);

        hBox.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(hBox, BIG_INSETS);

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
            dao().checkTransactions(transactions, check);
            Platform.runLater(() -> selected.ifPresent(record -> statementTable.getSelectionModel().select(record)));
        });

        cache().getAccounts().addListener(new WeakListChangeListener<>(accountListener));
        cache().getTransactions().addListener(new WeakListChangeListener<>(transactionListener));

        accountComboBox.getSelectionModel()
                .selectedItemProperty().addListener((_, _, newValue) -> calculateTransactions(newValue));

        setupWindow(root);
        setupAccountComboBox();
        settings().loadStageDimensions(this);
    }

    void onNewTransaction(StatementRecord statementRecord) {
        var account = accountComboBox.getSelectionModel().getSelectedItem();
        new TransactionDialog(this, settings().getDialogCssFileUrl(), statementRecord, account, cache()).showAndWait()
                .ifPresent(builder -> dao().insertTransaction(builder));
    }

    void onStatementRecordSelected(StatementRecord statementRecord) {
        transactionTable.setTransactionFilter(
                new StatementPredicate(accountComboBox.getValue(), statementRecord, ignoreExecutionDate.isSelected()));
    }

    @Override
    public String getTitle() {
        return "Выписки";
    }

    private Optional<Statement> getStatement() {
        return Optional.ofNullable(statement);
    }

    private MenuBar createMainMenu() {
        return menuBar(
                menu("Файл",
                        menuItem("Открыть...", SHORTCUT_O, _ -> onBrowse()),
                        new SeparatorMenuItem(),
                        menuItem("Отчет...", _ -> onReport()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)
                ),
                menu("Правка",
                        menuItem("Добавить...", SHORTCUT_N,
                                _ -> getSelectedStatementRecord().ifPresent(this::onNewTransaction)),
                        new SeparatorMenuItem(),
                        menuItem("Отметить", SHORTCUT_K, _ -> onCheckStatementRecord(true)),
                        menuItem("Снять отметку", SHORTCUT_U, _ -> onCheckStatementRecord(false))
                ),
                createWindowMenu(),
                createHelpMenu()
        );
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
        var dialog = new StatementFileDialog();
        dialog.show(getStage()).ifPresent(selected -> {
            setTitle(getTitle() + " - " + selected.getAbsolutePath());
            settings().update(opt -> opt.setLastStatementDir(selected.getParent()));

            StatementParser.parse(new RawStatementData(selected), cache())
                    .ifPresent(this::analyzeStatement);
        });
    }

    public void setStatement(String fileName, RawStatementData statementData) {
        setTitle(getTitle() + " - " + fileName);
        StatementParser.parse(statementData, cache())
                .ifPresent(this::analyzeStatement);
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

        // TODO: sort via table comparator
        statement.records().sort((o1, o2) -> o2.getActual().compareTo(o1.getActual()));

        Platform.runLater(() -> {
            var selected = statementTable.getSelectionModel().getSelectedItem();
            statementTable.getItems().setAll(statement.records());
            if (statement.records().contains(selected)) {
                statementTable.getSelectionModel().select(selected);
            }
        });
    }

    private void onReport() {
        new ReportFileDialog().show(getStage(), ReportType.STATEMENT).ifPresent(selected -> {
            try (var outputStream = new FileOutputStream(selected)) {
                getStatement().ifPresent(statement -> Reports.reportStatement(statement, outputStream));
                settings().update(opt -> opt.setLastReportDir(selected.getParent()));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
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
        dao().checkTransactions(transactions, check);
        Platform.runLater(() -> statementTable.getSelectionModel().select(record));
    }

    private TableView<StatementRecord> createStatementTable() {
        var tableView = new TableView<StatementRecord>();

        tableView.setRowFactory(_ -> new StatementRow());

        var w = tableView.widthProperty().subtract(20);
        tableView.getColumns().addAll(List.of(
                tableColumn("Дата", (TableColumnBuilder<StatementRecord, LocalDate> b) ->
                        b.withCellFactory(_ -> new LocalDateCell<>())
                                .withPropertyCallback(StatementRecord::getActual)
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn("Дата исп.", (TableColumnBuilder<StatementRecord,
                        LocalDate> b) ->
                        b.withCellFactory(_ -> new LocalDateCell<>())
                                .withPropertyCallback(StatementRecord::getExecution)
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn("Описание", b ->
                        b.withPropertyCallback(StatementRecord::getDescription).withWidthBinding(w.multiply(0.5))),
                tableColumn("Контрагент", b ->
                        b.withPropertyCallback(StatementRecord::getCounterParty).withWidthBinding(w.multiply(0.15))),
                tableColumn("Место", b ->
                        b.withPropertyCallback(StatementRecord::getPlace).withWidthBinding(w.multiply(0.10))),
                tableColumn("Страна", b ->
                        b.withPropertyCallback(StatementRecord::getCountry).withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Сумма", b ->
                        b.withCellFactory(_ -> new StatementSumCell()).withWidthBinding(w.multiply(0.1)))
        ));

        var menu = new ContextMenu();
        menu.getItems().addAll(menuItem("Добавить...",
                _ -> getSelectedStatementRecord().ifPresent(this::onNewTransaction)));
        tableView.setContextMenu(menu);

        tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) ->
                onStatementRecordSelected(newValue));

        return tableView;
    }

    private Optional<StatementRecord> getSelectedStatementRecord() {
        return Optional.ofNullable(statementTable.getSelectionModel().getSelectedItem());
    }
}
