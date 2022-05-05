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
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.panteleyev.freedesktop.Utility;
import org.panteleyev.freedesktop.entry.DesktopEntryBuilder;
import org.panteleyev.freedesktop.entry.DesktopEntryType;
import org.panteleyev.freedesktop.menu.Category;
import org.panteleyev.money.MoneyApplication;
import org.panteleyev.money.app.database.ConnectDialog;
import org.panteleyev.money.app.database.ConnectionProfile;
import org.panteleyev.money.app.database.ConnectionProfileManager;
import org.panteleyev.money.app.icons.IconWindowController;
import org.panteleyev.money.app.settings.SettingsDialog;
import org.panteleyev.money.bundles.UiBundle;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.xml.Export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
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

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.control.ButtonType.YES;
import static org.panteleyev.freedesktop.entry.LocaleString.localeString;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Constants.FILTER_ALL_FILES;
import static org.panteleyev.money.app.Constants.FILTER_XML_FILES;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_LEFT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_RIGHT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_UP;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_K;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.bundles.Internationalization.I18M_MISC_SCHEMA_RESET_HEADER;
import static org.panteleyev.money.bundles.Internationalization.I18N_CREATE_DESKTOP_ENTRY;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_CURRENT_MONTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_DELETE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EXIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EXPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ICONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_IMPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_NEXT_MONTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_OPTIONS;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_PREVIOUS_MONTH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_PROFILES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_REPORT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_UNCHECK;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_TOOLS;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_VIEW;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_INCOMPATIBLE_SCHEMA;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_SCHEMA_UPDATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_SCHEMA_UPDATE_TEXT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONNECTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DETAILS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REPORT;

public class MainWindowController extends BaseController implements TransactionTableView.TransactionDetailsCallback {
    public static final ResourceBundle UI = ResourceBundle.getBundle(UiBundle.class.getCanonicalName());

    private final BorderPane self = new BorderPane();

    private final Label progressLabel = label("");
    private final ProgressBar progressBar = new ProgressBar();

    private final SimpleBooleanProperty dbOpenProperty = new SimpleBooleanProperty(false);

    private final ConnectionProfileManager profileManager =
            new ConnectionProfileManager(this::onResetDatabase, this::onBuildDatasource);

    // Transaction view box
    private final ChoiceBox<String> monthFilterBox = new ChoiceBox<>();
    private final Spinner<Integer> yearSpinner = new Spinner<>();

    private final TransactionTableView transactionTable =
            new TransactionTableView(this, TransactionTableView.Mode.SUMMARY, this,
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
        super(stage, settings().getMainCssFilePath());

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
        var fileConnectMenuItem = menuItem(fxString(UI, I18N_WORD_CONNECTION, ELLIPSIS), SHORTCUT_N,
                event -> onOpenConnection());
        var fileCloseMenuItem = menuItem(fxString(UI, I18N_WORD_CLOSE), event -> onClose());
        var fileExitMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_EXIT), event -> onExit());
        var exportMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_EXPORT, ELLIPSIS), event -> xmlDump());
        var importMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_IMPORT, ELLIPSIS), event -> onImport());
        var reportMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_REPORT, ELLIPSIS), event -> onReport());

        var fileMenu = newMenu(fxString(UI, I18N_MENU_FILE),
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

        var editMenu = newMenu(fxString(UI, I18N_MENU_EDIT),
                menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                        event -> transactionTable.onNewTransaction()),
                menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E,
                        event -> transactionTable.onEditTransaction()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_DELETE, ELLIPSIS), SHORTCUT_DELETE,
                        event -> transactionTable.onDeleteTransaction()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_WORD_DETAILS, ELLIPSIS), x -> transactionTable.onTransactionDetails()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_CHECK), SHORTCUT_K,
                        event -> transactionTable.onCheckTransactions(true)),
                menuItem(fxString(UI, I18N_MENU_ITEM_UNCHECK), SHORTCUT_U,
                        event -> transactionTable.onCheckTransactions(false))
        );

        var viewMenu = newMenu(fxString(UI, I18N_MENU_VIEW),
                menuItem(fxString(UI, I18N_MENU_ITEM_CURRENT_MONTH), SHORTCUT_ALT_UP, x -> onCurrentMonth()),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_NEXT_MONTH), SHORTCUT_ALT_RIGHT, x -> onNextMonth()),
                menuItem(fxString(UI, I18N_MENU_ITEM_PREVIOUS_MONTH), SHORTCUT_ALT_LEFT, x -> onPrevMonth())
        );

        var profilesMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_PROFILES, ELLIPSIS),
                x -> profileManager.getEditor().showAndWait());

        var optionsMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_OPTIONS, ELLIPSIS),
                x -> onOptions());
        var iconWindowMenuItem = menuItem(fxString(UI, I18N_MENU_ITEM_ICONS, ELLIPSIS),
                x -> onIconWindow());

        var toolsMenu = newMenu(fxString(UI, I18N_MENU_TOOLS),
                profilesMenuItem,
                new SeparatorMenuItem(),
                iconWindowMenuItem,
                new SeparatorMenuItem(),
                optionsMenuItem,
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_CREATE_DESKTOP_ENTRY), a -> onCreateDesktopEntry())
        );

        var menuBar = new MenuBar(fileMenu, editMenu, viewMenu, toolsMenu,
                createWindowMenu(dbOpenProperty), createHelpMenu());

        menuBar.setUseSystemMenuBar(true);
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

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

        settings().loadStageDimensions(this);

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
                .toList()
                .forEach(c -> ((BaseController) c).onClose());
    }

    @Override
    public void onClose() {
        closeChildWindows();

        setTitle(AboutDialog.APP_TITLE);
        dao().initialize(null);
        dbOpenProperty.set(false);
    }

    private void onOpenConnection() {
        new ConnectDialog(profileManager).showAndWait()
                .ifPresent(this::open);
    }

    private void open(ConnectionProfile profile) {
        var ds = onBuildDatasource(profile);

        dao().initialize(ds);

        var schemaStatus = dao().checkSchemaUpdateStatus();
        switch (schemaStatus) {
            case UPDATE_REQUIRED -> {
                var alert = new Alert(WARNING, fxString(UI, I18N_MISC_SCHEMA_UPDATE_TEXT), YES, NO);
                alert.setHeaderText(fxString(UI, I18M_MISC_SCHEMA_RESET_HEADER));
                alert.setTitle(fxString(UI, I18N_MISC_SCHEMA_UPDATE));

                var confirmed = alert.showAndWait()
                        .filter(response -> response == YES)
                        .isPresent();

                if (confirmed) {
                    dao().updateSchema();
                } else {
                    System.exit(0);
                }
            }
            case INCOMPATIBLE -> {
                new Alert(ERROR, fxString(UI, I18N_MISC_INCOMPATIBLE_SCHEMA), OK).showAndWait();
                System.exit(-1);
            }
        }

        var loadResult = CompletableFuture
                .runAsync(() -> dao().preload())
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
        new SettingsDialog(this, settings()).showAndWait();
    }

    private void setTitle(String title) {
        getStage().setTitle(title);
    }

    @Override
    protected void onWindowHiding() {
        super.onWindowHiding();
        closeChildWindows();
        settings().saveWindowsSettings();
    }

    private void xmlDump() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");
        settings().getLastExportDir().ifPresent(dir -> {
            if (dir.exists() && dir.isDirectory()) {
                fileChooser.setInitialDirectory(dir);
            }
        });
        fileChooser.setInitialFileName(generateFileName());
        fileChooser.getExtensionFilters().addAll(FILTER_XML_FILES, FILTER_ALL_FILES);

        var selected = fileChooser.showSaveDialog(getStage());
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
                settings().update(opt -> opt.setLastExportDir(selected.getParent()));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void onImport() {
        new ImportWizard(this).showAndWait();
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(fxString(UI, I18N_WORD_REPORT));
        settings().getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("transactions"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(getStage());
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            var filter = transactionTable.getTransactionFilter();
            var transactions = cache().getTransactions(filter)
                    .sorted(cache().getTransactionByDateComparator())
                    .toList();
            Reports.reportTransactions(transactions, outputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Exception onResetDatabase(ConnectionProfile profile) {
        var ds = onBuildDatasource(profile);
        return MoneyDAO.resetDatabase(ds, profile.schema());
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

    private void setCurrentDate() {
        var now = LocalDate.now();
        monthFilterBox.getSelectionModel().select(now.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(now.getYear());
    }

    private void goToTransaction(Transaction transaction) {
        transactionTable.getSelectionModel().clearSelection();
        var date = Transaction.getDate(transaction);
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
        dao().checkTransactions(transactions, check);
    }

    @Override
    public void handleTransactionDetails(Transaction transaction, List<TransactionDetail> details) {
        var childTransactions = cache().getTransactionDetails(transaction);

        if (details.isEmpty()) {
            if (!childTransactions.isEmpty()) {
                dao().deleteTransactions(childTransactions);
                var noChildren = new Transaction.Builder(transaction)
                        .detailed(false)
                        .timestamp()
                        .build();
                dao().updateTransaction(noChildren);
            }
        } else {
            dao().updateTransaction(new Transaction.Builder(transaction)
                    .detailed(true)
                    .timestamp()
                    .build());

            dao().deleteTransactions(childTransactions);

            for (var transactionDetail : details) {
                var newDetail = new Transaction.Builder(transaction)
                        .accountCreditedUuid(transactionDetail.accountCreditedUuid())
                        .amount(transactionDetail.amount())
                        .comment(transactionDetail.comment())
                        .uuid(UUID.randomUUID())
                        .parentUuid(transaction.uuid())
                        .detailed(false)
                        .timestamp()
                        .build();
                dao().insertTransaction(newDetail);
            }
        }
    }

    private void onCreateDesktopEntry() {
        if (!Utility.isLinux()) {
            return;
        }
        Utility.getExecutablePath().ifPresent(command -> {
            var execFile = new File(command);
            var rootDir = execFile.getParentFile().getParentFile().getAbsolutePath();

            var desktopEntry = new DesktopEntryBuilder(DesktopEntryType.APPLICATION)
                    .version(DesktopEntryBuilder.VERSION_1_0)
                    .name(localeString("Money Manager"))
                    .name(localeString("Менеджер финансов", "ru_RU"))
                    .categories(List.of(Category.OFFICE, Category.FINANCE, Category.JAVA))
                    .comment(localeString("Application to manage personal finances"))
                    .comment(localeString("Программа для управления личными финансами", "ru_RU"))
                    .exec("\"" + command + "\"")
                    .icon(localeString(rootDir + "/lib/Money Manager.png"))
                    .build();
            desktopEntry.write("money-manager");
        });
    }
}
