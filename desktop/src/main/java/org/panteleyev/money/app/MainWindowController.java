/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

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
import org.panteleyev.money.app.dialogs.ExportFileFialog;
import org.panteleyev.money.app.dialogs.ReportFileDialog;
import org.panteleyev.money.app.icons.IconWindowController;
import org.panteleyev.money.app.settings.SettingsDialog;
import org.panteleyev.money.app.transaction.TransactionTableView;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionDetail;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.xml.Export;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.zip.ZipOutputStream;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;
import static javafx.scene.control.ButtonType.NO;
import static javafx.scene.control.ButtonType.OK;
import static javafx.scene.control.ButtonType.YES;
import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.freedesktop.Utility.isLinux;
import static org.panteleyev.freedesktop.entry.DesktopEntryBuilder.localeString;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_I;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_LEFT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_P;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_R;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_RIGHT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_S;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_UP;

public class MainWindowController extends BaseController implements TransactionTableView.TransactionDetailsCallback {
    public static final ResourceBundle UI = new ListResourceBundle() {
        @Override
        protected Object[][] getContents() {
            return new Object[][]{
                    {"button.Cancel", "Отмена"}
            };
        }
    };

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
    private final ListChangeListener<Account> accountListener = _ -> Platform.runLater(this::reloadTransactions);

    public static final Validator<String> BIG_DECIMAL_VALIDATOR = (Control control, String value) -> {
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
        var fileConnectMenuItem = menuItem("Соединение...", _ -> onOpenConnection());
        var fileExitMenuItem = menuItem("Выход", _ -> onExit());
        var exportMenuItem = menuItem("Экспорт...", SHORTCUT_ALT_E, _ -> xmlDump());
        var importMenuItem = menuItem("Импорт...", SHORTCUT_ALT_I, _ -> onImport());
        var reportMenuItem = menuItem("Отчет...", SHORTCUT_ALT_R, _ -> onReport());

        var fileMenu = menu("Файл",
                fileConnectMenuItem,
                new SeparatorMenuItem(),
                importMenuItem,
                exportMenuItem,
                new SeparatorMenuItem(),
                reportMenuItem,
                new SeparatorMenuItem(),
                createMenuItem(ACTION_CLOSE),
                new SeparatorMenuItem(),
                fileExitMenuItem);

        var editMenu = createMenu("Правка", transactionTable.getActions());

        var viewMenu = menu("Вид",
                menuItem("Текущий месяц", SHORTCUT_ALT_UP, _ -> onCurrentMonth()),
                new SeparatorMenuItem(),
                menuItem("Следующий месяц", SHORTCUT_ALT_RIGHT, _ -> onNextMonth()),
                menuItem("Предыдущий месяц", SHORTCUT_ALT_LEFT, _ -> onPrevMonth())
        );

        var profilesMenuItem = menuItem("Профили...", SHORTCUT_ALT_P,
                _ -> profileManager.getEditor().showAndWait());

        var optionsMenuItem = menuItem("Настройки...",
                SHORTCUT_ALT_S, _ -> onOptions());
        var iconWindowMenuItem = menuItem("Значки...",
                _ -> onIconWindow());

        var toolsMenu = menu("Сервис",
                profilesMenuItem,
                new SeparatorMenuItem(),
                iconWindowMenuItem,
                new SeparatorMenuItem(),
                optionsMenuItem,
                isLinux() ? new SeparatorMenuItem() : null,
                isLinux() ? menuItem("Создать ярлык приложения", _ -> onCreateDesktopEntry()) : null
        );

        var menuBar = new MenuBar(fileMenu, editMenu, viewMenu, toolsMenu,
                createPortfolioMenu(dbOpenProperty),
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

        monthFilterBox.setOnAction(_ -> onMonthChanged());
        monthFilterBox.setFocusTraversable(false);

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
        yearSpinner.valueProperty().addListener((_, _, _) -> Platform.runLater(this::reloadTransactions));
        yearSpinner.setFocusTraversable(false);

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
                var alert = new Alert(WARNING, "Требуется совместимое обновление базы данных. Продолжать?", YES, NO);
                alert.setHeaderText("Внимание");
                alert.setTitle("Обновление базы данных");

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
                new Alert(ERROR, "База данных несовместима с приложением, завершение работы.", OK).showAndWait();
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
        new ExportFileFialog().showExportDialog(getStage()).ifPresent(selected -> {
            CompletableFuture.runAsync(() -> {
                try (var outputStream = new ZipOutputStream(new FileOutputStream(selected))) {
                    new Export().doExport(outputStream);
                    settings().update(opt -> opt.setLastExportDir(selected.getParent()));
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        });
    }

    private void onImport() {
        new ImportWizard(this).showAndWait();
    }

    private void onReport() {
        new ReportFileDialog().show(getStage(), ReportType.TRANSACTIONS).ifPresent(selected -> {
            try (var outputStream = new FileOutputStream(selected)) {
                var filter = transactionTable.getTransactionFilter();
                var transactions = cache().getTransactions(filter)
                        .sorted(Comparators.transactionsByDate())
                        .toList();
                Reports.reportTransactions(transactions, outputStream);
                settings().update(opt -> opt.setLastReportDir(selected.getParent()));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private Exception onResetDatabase(ConnectionProfile profile) {
        var ds = onBuildDatasource(profile);
        return MoneyDAO.resetDatabase(ds, profile.schema());
    }

    private PGSimpleDataSource onBuildDatasource(ConnectionProfile profile) {
        var ds = new PGSimpleDataSource();
        ds.setServerNames(new String[]{profile.dataBaseHost()});
        ds.setPortNumbers(new int[]{profile.dataBasePort()});
        ds.setUser(profile.dataBaseUser());
        ds.setPassword(profile.dataBasePassword());
        ds.setDatabaseName(profile.databaseName());
        ds.setCurrentSchema(profile.schema());
        return ds;
    }

    private void setCurrentDate() {
        var now = LocalDate.now();
        monthFilterBox.getSelectionModel().select(now.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(now.getYear());
    }

    private void goToTransaction(Transaction transaction) {
        transactionTable.getSelectionModel().clearSelection();
        monthFilterBox.getSelectionModel().select(transaction.transactionDate().getMonthValue() - 1);
        yearSpinner.getValueFactory().setValue(transaction.transactionDate().getYear());
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

        Predicate<Transaction> filter = t -> t.transactionDate().getMonthValue() == month
                && t.transactionDate().getYear() == year;
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
        if (!isLinux()) {
            return;
        }
        Utility.getExecutablePath().ifPresent(command -> {
            var execFile = new File(command);
            var rootDir = execFile.getParentFile().getParentFile().getAbsolutePath();

            var desktopEntry = new DesktopEntryBuilder(DesktopEntryType.APPLICATION)
                    .version(DesktopEntryBuilder.VERSION_1_5)
                    .name("Money Manager")
                    .name(localeString("Менеджер финансов", "ru_RU"))
                    .categories(List.of(Category.OFFICE, Category.FINANCE, Category.JAVA))
                    .comment("Application to manage personal finances")
                    .comment(localeString("Программа для управления личными финансами", "ru_RU"))
                    .exec("\"" + command + "\"")
                    .icon(rootDir + "/lib/Money Manager.png")
                    .build();
            desktopEntry.write("money-manager");
        });
    }
}
