/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.MoneyApplication;
import org.panteleyev.money.app.database.ConnectDialog;
import org.panteleyev.money.app.database.ConnectionProfile;
import org.panteleyev.money.app.database.ConnectionProfileManager;
import org.panteleyev.money.app.icons.IconWindowController;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.xml.Export;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Constants.ELLIPSIS;
import static org.panteleyev.money.app.Constants.FILTER_ALL_FILES;
import static org.panteleyev.money.app.Constants.FILTER_XML_FILES;
import static org.panteleyev.money.app.Constants.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_K;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.Constants.SHORTCUT_U;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class MainWindowController extends BaseController implements TransactionTableView.TransactionDetailsCallback {
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(MainWindowController.class);

    private static final String UI_BUNDLE_PATH = "org.panteleyev.money.app.res.ui";
    public static final URL CSS_PATH = MainWindowController.class.getResource("/org/panteleyev/money/app/res/main.css");

    public static final ResourceBundle RB = ResourceBundle.getBundle(UI_BUNDLE_PATH);

    private final BorderPane self = new BorderPane();

    private final Label progressLabel = label("");
    private final ProgressBar progressBar = new ProgressBar();

    private final SimpleBooleanProperty dbOpenProperty = new SimpleBooleanProperty(false);

    private final ConnectionProfileManager profileManager =
        new ConnectionProfileManager(this::onInitDatabase, this::onBuildDatasource,
            PREFERENCES);

    // Transaction view box
    private final ChoiceBox<String> monthFilterBox = new ChoiceBox<>();
    private final Spinner<Integer> yearSpinner = new Spinner<>();

    private final TransactionTableView transactionTable =
        new TransactionTableView(TransactionTableView.Mode.ACCOUNT, this,
            this::goToTransaction, this::goToTransaction);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Account> accountListener =
        change -> Platform.runLater(this::reloadTransactions);

    static final Validator<String> BIG_DECIMAL_VALIDATOR = (Control control, String value) -> {
        boolean invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    public MainWindowController(Stage stage) {
        super(stage, CSS_PATH.toString());

        profileManager.loadProfiles();

        stage.getIcons().add(Images.APP_ICON);
        initialize();
        setupWindow(self);
    }

    @Override
    public String getTitle() {
        return "Money Manager";
    }

    private MenuBar createMainMenu() {
        // Main menu
        var fileConnectMenuItem = menuItem(fxString(RB, "Connection", ELLIPSIS), SHORTCUT_N,
            event -> onOpenConnection());
        var fileCloseMenuItem = menuItem(fxString(RB, "Close"), event -> onClose());
        var fileExitMenuItem = menuItem(fxString(RB, "Exit"), event -> onExit());
        var exportMenuItem = menuItem(fxString(RB, "menu.Tools.Export"), event -> xmlDump());
        var importMenuItem = menuItem(fxString(RB, "word.Import", ELLIPSIS), event -> onImport());
        var reportMenuItem = menuItem(fxString(RB, "Report", ELLIPSIS), event -> onReport());

        var fileMenu = newMenu(fxString(RB, "File"),
            fileConnectMenuItem,
            new SeparatorMenuItem(),
            importMenuItem,
            exportMenuItem,
            new SeparatorMenuItem(),
            reportMenuItem,
            new SeparatorMenuItem(),
            fileCloseMenuItem,
            new SeparatorMenuItem(),
            fileExitMenuItem);

        var editMenu = newMenu(fxString(RB, "menu.Edit"),
            menuItem(fxString(RB, "Add", ELLIPSIS), SHORTCUT_N,
                event -> transactionTable.onNewTransaction()),
            menuItem(fxString(RB, "Edit", ELLIPSIS), SHORTCUT_E,
                event -> transactionTable.onEditTransaction()),
            new SeparatorMenuItem(),
            menuItem(fxString(RB, "Delete", ELLIPSIS), SHORTCUT_DELETE,
                event -> transactionTable.onDeleteTransaction()),
            new SeparatorMenuItem(),
            menuItem(fxString(RB, "menu.item.details"), x -> transactionTable.onTransactionDetails()),
            new SeparatorMenuItem(),
            menuItem(fxString(RB, "menu.item.check"), SHORTCUT_K,
                event -> transactionTable.onCheckTransactions(true)),
            menuItem(fxString(RB, "menu.item.uncheck"), SHORTCUT_U,
                event -> transactionTable.onCheckTransactions(false))
        );

        var viewMenu = newMenu(fxString(RB, "menu.view"),
            menuItem(fxString(RB, "menu.view.currentMonth"),
                new KeyCodeCombination(KeyCode.UP, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN),
                x -> onCurrentMonth()),
            new SeparatorMenuItem(),
            menuItem(fxString(RB, "menu.view.nextMonth"),
                new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN),
                x -> onNextMonth()),
            menuItem(fxString(RB, "menu.view.prevMonth"),
                new KeyCodeCombination(KeyCode.LEFT, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN),
                x -> onPrevMonth())
        );

        var profilesMenuItem = menuItem(fxString(RB, "menu.Tools.Profiles"),
            x -> profileManager.getEditor(false).showAndWait());

        var optionsMenuItem = menuItem(fxString(RB, "menu.Tools.Options"), x -> onOptions());
        var importSettingsMenuItem = menuItem(fxString(RB, "menu.tools.import.settings"), x -> onImportSettings());
        var exportSettingsMenuItem = menuItem(fxString(RB, "menu.tool.export.settings"), x -> onExportSettings());
        var iconWindowMenuItem = menuItem(fxString(RB.getString("string.icons"), ELLIPSIS), x -> onIconWindow());

        var toolsMenu = newMenu(fxString(RB, "menu.Tools"),
            profilesMenuItem,
            new SeparatorMenuItem(),
            iconWindowMenuItem,
            new SeparatorMenuItem(),
            optionsMenuItem,
            importSettingsMenuItem,
            exportSettingsMenuItem
        );

        var menuBar = new MenuBar(fileMenu, editMenu, viewMenu, toolsMenu,
            createWindowMenu(dbOpenProperty), createHelpMenu());

        menuBar.setUseSystemMenuBar(true);

        iconWindowMenuItem.disableProperty().bind(dbOpenProperty.not());

        exportMenuItem.disableProperty().bind(dbOpenProperty.not());
        importMenuItem.disableProperty().bind(dbOpenProperty.not());
        reportMenuItem.disableProperty().bind(dbOpenProperty.not());

        return menuBar;
    }

    private void initialize() {
        self.setTop(createMainMenu());

        var transactionTab = new BorderPane();

        monthFilterBox.setOnAction(event -> onMonthChanged());

        var transactionCountLabel = label("");
        transactionCountLabel.textProperty().bind(transactionTable.listSizeProperty().asString());

        var hBox = hBox(5.0,
            monthFilterBox,
            yearSpinner,
            fxNode(new Region(), hBoxHGrow(Priority.ALWAYS)),
            label("Transactions:"),
            transactionCountLabel
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        transactionTab.setTop(hBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        transactionTab.setCenter(transactionTable);

        for (int i = 1; i <= 12; i++) {
            monthFilterBox.getItems()
                .add(Month.of(i).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
        }

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory
            .IntegerSpinnerValueFactory(1970, 2050);
        yearSpinner.setValueFactory(valueFactory);
        yearSpinner.valueProperty().addListener((x, y, z) -> Platform.runLater(this::reloadTransactions));

        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        setCurrentDate();

        cache().getAccounts().addListener(new WeakListChangeListener<>(accountListener));

        self.setCenter(transactionTab);
        self.setBottom(new HBox(progressLabel, progressBar));

        HBox.setMargin(progressLabel, new Insets(0.0, 0.0, 0.0, 5.0));
        HBox.setMargin(progressBar, new Insets(0.0, 0.0, 0.0, 5.0));

        progressLabel.setVisible(false);
        progressBar.setVisible(false);

        getStage().setOnHiding(event -> onWindowClosing());

        getStage().setWidth(Options.getMainWindowWidth());
        getStage().setHeight(Options.getMainWindowHeight());

        profileManager.getProfileToOpen().ifPresent(this::open);
    }

    private void onIconWindow() {
        getController(IconWindowController.class);
    }

    private void onExit() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void closeChildWindows() {
        WINDOW_MANAGER.getControllerStream()
            .filter(c -> c != this)
            .collect(Collectors.toList())
            .forEach(c -> ((BaseController) c).onClose());
    }

    @Override
    public void onClose() {
        closeChildWindows();

        setTitle(AboutDialog.APP_TITLE);
        getDao().initialize(null);
        dbOpenProperty.set(false);
    }

    private void onOpenConnection() {
        new ConnectDialog(profileManager).showAndWait()
            .ifPresent(this::open);
    }

    private void open(ConnectionProfile profile) {
        var ds = onBuildDatasource(profile);

        getDao().initialize(ds);

        var loadResult = CompletableFuture
            .runAsync(() -> getDao().preload())
            .thenRun(() -> Platform.runLater(() -> {
                setTitle(AboutDialog.APP_TITLE + " - " + profile.name() + " - " + profile.getConnectionString());
                dbOpenProperty.set(true);
            }));

        checkFutureException(loadResult);
    }

    private void checkFutureException(Future<?> f) {
        try {
            f.get();
        } catch (ExecutionException | InterruptedException ex) {
            MoneyApplication.uncaughtException(ex.getCause());
        }
    }

    private void onOptions() {
        new OptionsDialog(this).showAndWait();
    }

    private void setTitle(String title) {
        getStage().setTitle(title);
    }

    private void onWindowClosing() {
        closeChildWindows();

        Options.setMainWindowWidth(getStage().widthProperty().doubleValue());
        Options.setMainWindowHeight(getStage().heightProperty().doubleValue());
    }

    private void xmlDump() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName());
        fileChooser.getExtensionFilters().addAll(FILTER_XML_FILES, FILTER_ALL_FILES);

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (var outputStream = new FileOutputStream(selected)) {
                new Export()
                    .withIcons(cache().getIcons())
                    .withCategories(cache().getCategories(), false)
                    .withAccounts(cache().getAccounts(), false)
                    .withCurrencies(cache().getCurrencies())
                    .withContacts(cache().getContacts(), false)
                    .withTransactions(cache().getTransactions(), false)
                    .doExport(outputStream);
                Options.setLastExportDir(selected.getParent());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void onImport() {
        new ImportWizard().showAndWait();
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(RB.getString("Report"));
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("transactions"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            var filter = transactionTable.getTransactionFilter();
            var transactions = cache().getTransactions(filter)
                .sorted(MoneyDAO.COMPARE_TRANSACTION_BY_DATE)
                .collect(Collectors.toList());
            Reports.reportTransactions(transactions, outputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Exception onInitDatabase(ConnectionProfile profile) {
        var ds = onBuildDatasource(profile);
        return MoneyDAO.initDatabase(ds, profile.schema());
    }

    private MysqlDataSource onBuildDatasource(ConnectionProfile profile) {
        try {
            var ds = new MysqlDataSource();

            ds.setCharacterEncoding("utf8");
            ds.setUseSSL(false);
            ds.setServerTimezone(TimeZone.getDefault().getID());
            ds.setPort(profileManager.getDatabasePort(profile));
            ds.setServerName(profileManager.getDatabaseHost(profile));
            ds.setUser(profile.dataBaseUser());
            ds.setPassword(profile.dataBasePassword());
            ds.setDatabaseName(profile.schema());
            ds.setAllowPublicKeyRetrieval(true);

            return ds;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void onImportSettings() {
        var d = new FileChooser();
        d.setTitle("Import Settings");
        d.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Settings", "*.xml")
        );
        var file = d.showOpenDialog(null);
        if (file != null) {
            try (var in = new FileInputStream(file)) {
                Preferences.importPreferences(in);
                profileManager.loadProfiles();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void onExportSettings() {
        var d = new FileChooser();
        d.setTitle("Export Settings");
        d.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Settings", "*.xml")
        );
        var file = d.showSaveDialog(null);
        if (file != null) {
            try (var out = new FileOutputStream(file)) {
                PREFERENCES.exportSubtree(out);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void setCurrentDate() {
        var now = LocalDate.now();
        monthFilterBox.getSelectionModel().select(now.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(now.getYear());
    }

    private void goToTransaction(Transaction transaction) {
        transactionTable.getSelectionModel().clearSelection();
        var date = transaction.getDate();
        monthFilterBox.getSelectionModel().select(date.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(date.getYear());
        transactionTable.getSelectionModel().select(transaction);
        transactionTable.scrollTo(transaction);
    }

    private void onPrevMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() - 1;

        if (month < 0) {
            month = 11;
            yearSpinner.getValueFactory().decrement(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onNextMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;

        if (month == 12) {
            month = 0;
            yearSpinner.getValueFactory().increment(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onCurrentMonth() {
        setCurrentDate();

        reloadTransactions();
    }

    private void reloadTransactions() {
        transactionTable.getSelectionModel().clearSelection();

        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        Predicate<Transaction> filter = t -> t.month() == month
            && t.year() == year;
//            && t.getParentId() == 0;

        transactionTable.setTransactionFilter(filter);
    }

    private void onMonthChanged() {
        reloadTransactions();
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (var t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
    }

    @Override
    public void handleTransactionDetails(Transaction transaction, List<TransactionDetail> details) {
        var childTransactions = cache().getTransactionDetails(transaction);

        if (details.isEmpty()) {
            if (!childTransactions.isEmpty()) {
                for (Transaction child : childTransactions) {
                    getDao().deleteTransaction(child);
                }
                var noChildren = new Transaction.Builder(transaction)
                    .detailed(false)
                    .timestamp()
                    .build();
                getDao().updateTransaction(noChildren);
            }
        } else {
            getDao().updateTransaction(new Transaction.Builder(transaction)
                .detailed(true)
                .timestamp()
                .build());

            for (Transaction ch : childTransactions) {
                getDao().deleteTransaction(ch);
            }

            for (var transactionDetail : details) {
                var newDetail = new Transaction.Builder(transaction)
                    .accountCreditedUuid(transactionDetail.accountCreditedUuid())
                    .amount(transactionDetail.amount())
                    .comment(transactionDetail.comment())
                    .guid(UUID.randomUUID())
                    .parentUuid(transaction.uuid())
                    .detailed(false)
                    .timestamp()
                    .build();
                getDao().insertTransaction(newDetail);
            }
        }
    }
}
