/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Constants.ELLIPSIS;
import static org.panteleyev.money.app.Constants.SHORTCUT_K;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.Constants.SHORTCUT_U;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class StatementWindowController extends BaseController {
    private static final ResourceBundle SOURCE_TYPE_RB =
        ResourceBundle.getBundle("org.panteleyev.money.app.res.SourceType");

    private enum SourceType {
        FILE,
        YANDEX_MONEY;

        public String toString() {
            return SOURCE_TYPE_RB.getString(name());
        }
    }

    private final TableView<StatementRecord> statementTable = createStatementTable();
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
    private final ListChangeListener<Account> accountListener = c -> Platform.runLater(this::setupAccountComboBox);
    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Transaction> transactionListener = c -> calculateTransactions();


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
        var fileLoadControls = hBox(5.0,
            statementFileEdit,
            button("...", x -> onBrowse())
        );
        fileLoadControls.visibleProperty().bind(
            sourceTypeComboBox.getSelectionModel().selectedItemProperty().isEqualTo(SourceType.FILE));
        fileLoadControls.setAlignment(Pos.CENTER_LEFT);
        ////////////////////////////////////////////////////////

        // Yandex Money controls
        var ymAuthButton = button("Authorize...", x -> yandexMoneyClient.authorize());

        limitComboBox.getSelectionModel().selectFirst();
        var yandexMoneyControls = hBox(5.0,
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

        var balanceBox = hBox(5.0,
            label(fxString(RB, "label.StatementBalance")),
            ymAccountBalanceLabel);
        balanceBox.setAlignment(Pos.CENTER_LEFT);

        var filler1 = new Region();

        var hBox = hBox(5.0,
            label(fxString(RB, "label.Account")),
            accountComboBox,
            button(fxString(RB, "button.Load"), x -> onLoad()),
            button(fxString(RB, "button.Clear"), x -> onClear()),
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

        //transactionTable.setFocusTraversable(false);

        accountComboBox.setConverter(new ReadOnlyNamedConverter<>());

        var lowerBox = hBox(5.0, sourceTypeComboBox, stackPane);

        var toolBar = vBox(5.0, lowerBox, hBox);
        BorderPane.setMargin(toolBar, new Insets(5.0, 5.0, 5.0, 5.0));

        var centerBox = new BorderPane();
        centerBox.setTop(toolBar);
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
        Options.loadStageDimensions(getClass(), getStage());
    }

    void onNewTransaction(StatementRecord statementRecord) {
        var account = accountComboBox.getSelectionModel().getSelectedItem();
        new TransactionDialog(statementRecord, account, cache()).showAndWait()
            .ifPresent(builder -> getDao().insertTransaction(builder));
    }

    void onStatementRecordSelected(StatementRecord statementRecord) {
        transactionTable.setTransactionFilter(
            new StatementPredicate(accountComboBox.getValue(), statementRecord, ignoreExecutionDate.isSelected()));
    }

    @Override
    public String getTitle() {
        return RB.getString("Statements");
    }

    private Optional<Statement> getStatement() {
        return Optional.ofNullable(statement);
    }

    private MenuBar createMainMenu() {
        return menuBar(
            newMenu(fxString(RB, "File"),
                menuItem(fxString(RB, "Report", ELLIPSIS), event -> onReport()),
                new SeparatorMenuItem(),
                menuItem(fxString(RB, "Close"), event -> onClose())),
            newMenu(fxString(RB, "Edit"),
                menuItem(fxString(RB, "menu.Edit.Add"), SHORTCUT_N,
                    event -> getSelectedStatementRecord().ifPresent(this::onNewTransaction)),
                new SeparatorMenuItem(),
                menuItem(fxString(RB, "menu.item.check"), SHORTCUT_K,
                    event -> onCheckStatementRecord(true)),
                menuItem(fxString(RB, "menu.item.uncheck"), SHORTCUT_U,
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
            .collect(Collectors.toList());

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
            case FILE -> {
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
            }
            case YANDEX_MONEY -> {
                var statement = yandexMoneyClient.load(limitComboBox.getSelectionModel().getSelectedItem(),
                    ymFromPicker.getValue(), ymToPicker.getValue());
                analyzeStatement(statement);
            }
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
        statementFileEdit.setText("");
        statementType = Statement.StatementType.UNKNOWN;
        statementTable.getItems().clear();
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

        for (var record : statement.records()) {
            record.setTransactions(cache().getTransactions().stream()
                .filter(new StatementPredicate(account, record, ignoreExecutionDate.isSelected()))
                .collect(Collectors.toList()));
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
        fileChooser.setTitle(RB.getString("Report"));
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
            tableColumn(fxString(RB, "column.Date"), (TableColumnBuilder<StatementRecord, LocalDate> b) ->
                b.withCellFactory(x -> new LocalDateCell<>())
                    .withPropertyCallback(StatementRecord::getActual)
                    .withWidthBinding(w.multiply(0.05))),
            tableColumn(fxString(RB, "column.ExecutionDate"), (TableColumnBuilder<StatementRecord, LocalDate> b) ->
                b.withCellFactory(x -> new LocalDateCell<>())
                    .withPropertyCallback(StatementRecord::getExecution)
                    .withWidthBinding(w.multiply(0.05))),
            tableColumn(fxString(RB, "Description"), b ->
                b.withPropertyCallback(StatementRecord::getDescription).withWidthBinding(w.multiply(0.5))),
            tableColumn(fxString(RB, "Counterparty"), b ->
                b.withPropertyCallback(StatementRecord::getCounterParty).withWidthBinding(w.multiply(0.15))),
            tableColumn(fxString(RB, "column.Place"), b ->
                b.withPropertyCallback(StatementRecord::getPlace).withWidthBinding(w.multiply(0.10))),
            tableColumn(fxString(RB, "Country"), b ->
                b.withPropertyCallback(StatementRecord::getCountry).withWidthBinding(w.multiply(0.05))),
            tableObjectColumn(fxString(RB, "column.Sum"), b ->
                b.withCellFactory(x -> new StatementSumCell()).withWidthBinding(w.multiply(0.1)))
        ));

        var menu = new ContextMenu();
        menu.getItems().addAll(menuItem(fxString(RB, "menu.Edit.Add"),
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
