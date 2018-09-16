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

package org.panteleyev.money;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.charts.ChartsTab;
import org.panteleyev.money.profiles.ConnectDialog;
import org.panteleyev.money.profiles.ConnectionProfile;
import org.panteleyev.money.profiles.ConnectionProfileManager;
import org.panteleyev.money.profiles.ConnectionProfilesEditor;
import org.panteleyev.money.ssh.SshManager;
import org.panteleyev.money.statements.StatementTab;
import org.panteleyev.money.xml.Export;
import org.panteleyev.utilities.fx.Controller;
import org.panteleyev.utilities.fx.WindowManager;
import javax.sql.DataSource;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class MainWindowController extends BaseController {
    private static final Logger LOGGER = Logger.getLogger(MainWindowController.class.getName());

    private static final String UI_BUNDLE_PATH = "org.panteleyev.money.res.ui";
    public static final String CSS_PATH = "/org/panteleyev/money/res/main.css";

    public static final ResourceBundle RB = ResourceBundle.getBundle(UI_BUNDLE_PATH);

    private final BorderPane self = new BorderPane();
    private final TabPane tabPane = new TabPane();

    private Label progressLabel = new Label();
    private ProgressBar progressBar = new ProgressBar();

    private Menu windowMenu = new Menu(RB.getString("menu.Window"));

    private final AccountsTab accountsTab = new AccountsTab();
    private TransactionsTab transactionTab = new TransactionsTab();
    private RequestTab requestTab = new RequestTab();
    private final StatementTab statementsTab = new StatementTab();
    private final ChartsTab chartsTab = new ChartsTab();

    private SimpleBooleanProperty dbOpenProperty = new SimpleBooleanProperty(false);

    static final Validator<String> BIG_DECIMAL_VALIDATOR = (Control control, String value) -> {
        boolean invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    private static final List<Class<? extends Controller>> WINDOW_CLASSES = List.of(
            ContactListWindowController.class,
            AccountListWindowController.class,
            CategoryWindowController.class,
            CurrencyWindowController.class
    );

    public MainWindowController(Stage stage) {
        super(stage, CSS_PATH);

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
        var m2 = new MenuItem(RB.getString("menu.File.Connect"));
        m2.setOnAction(event -> onOpenConnection());
        var m3 = new MenuItem(RB.getString("menu.File.Close"));
        m3.setOnAction(event -> onClose());
        var m4 = new MenuItem(RB.getString("menu.File.Exit"));
        m4.setOnAction(event -> onExit());

        var fileMenu = new Menu(RB.getString("menu.File"), null,
                m2, new SeparatorMenuItem(), m3, new SeparatorMenuItem(), m4);

        var m5 = new MenuItem(RB.getString("menu.Edit.Delete"));

        var currenciesMenuItem = new MenuItem(RB.getString("menu.Edit.Currencies"));
        currenciesMenuItem.setOnAction(event -> onManageCurrencies());
        var categoriesMenuItem = new MenuItem(RB.getString("menu.Edit.Categories"));
        categoriesMenuItem.setOnAction(event -> onManageCategories());
        var accountsMenuItem = new MenuItem(RB.getString("menu.Edit.Accounts"));
        accountsMenuItem.setOnAction(event -> onManageAccounts());
        var contactsMenuItem = new MenuItem(RB.getString("menu.Edit.Contacts"));
        contactsMenuItem.setOnAction(event -> onManageContacts());

        var editMenu = new Menu(RB.getString("menu.Edit"), null,
                m5, new SeparatorMenuItem(),
                currenciesMenuItem, categoriesMenuItem, accountsMenuItem, contactsMenuItem);

        var dumpXmlMenuItem = new MenuItem(RB.getString("menu.Tools.Export"));
        dumpXmlMenuItem.setOnAction(event -> xmlDump());

        var importMenuItem = new MenuItem(RB.getString("word.Import") + "...");
        importMenuItem.setOnAction(event -> onImport());

        var profilesMenuItem = new MenuItem(RB.getString("menu.Tools.Profiles"));
        profilesMenuItem.setOnAction(event -> onProfiles());

        var m7 = new MenuItem(RB.getString("menu.Tools.Options"));
        m7.setOnAction(event -> onOptions());

        var toolsMenu = new Menu(RB.getString("menu.Tools"), null,
                dumpXmlMenuItem,
                importMenuItem,
                new SeparatorMenuItem(),
                profilesMenuItem,
                new SeparatorMenuItem(),
                m7
        );

        /* Dummy menu item is required in order to let onShowing() fire up first time */
        windowMenu.getItems().setAll(new MenuItem("dummy"));

        var menuBar = new MenuBar(fileMenu, editMenu, toolsMenu,
                windowMenu, createHelpMenu(RB));

        menuBar.setUseSystemMenuBar(true);

        currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
        categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
        accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
        contactsMenuItem.disableProperty().bind(dbOpenProperty.not());

        dumpXmlMenuItem.disableProperty().bind(dbOpenProperty.not());
        importMenuItem.disableProperty().bind(dbOpenProperty.not());

        return menuBar;
    }

    private void initialize() {
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        self.setTop(createMainMenu());
        self.setCenter(tabPane);
        self.setBottom(new HBox(progressLabel, progressBar));

        HBox.setMargin(progressLabel, new Insets(0.0, 0.0, 0.0, 5.0));
        HBox.setMargin(progressBar, new Insets(0.0, 0.0, 0.0, 5.0));

        progressLabel.setVisible(false);
        progressBar.setVisible(false);

        var t2 = new Tab(RB.getString("tab.Transactions"), transactionTab);
        t2.selectedProperty().addListener((x, y, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> transactionTab.scrollToEnd());
            }
        });

        tabPane.getTabs().addAll(
                new Tab(RB.getString("tab.Accouts"), accountsTab),
                t2, new Tab(RB.getString("tab.Requests"), requestTab),
                new Tab(RB.getString("Statement"), statementsTab),
                new Tab(RB.getString("tab.Charts"), chartsTab)
        );

        statementsTab.setNewTransactionCallback((record, account) -> {
            tabPane.getSelectionModel().select(t2);
            transactionTab.handleStatementRecord(record, account);
        });

        windowMenu.setOnShowing(event -> {
            windowMenu.getItems().clear();

            windowMenu.getItems().add(new MenuItem("Money Manager"));
            windowMenu.getItems().add(new SeparatorMenuItem());

            WindowManager.getFrames().forEach(frame -> {
                if (frame != this) {
                    var item = new MenuItem(frame.getTitle());
                    item.setOnAction(action -> frame.getStage().toFront());
                    windowMenu.getItems().add(item);
                }
            });
        });

        getStage().setOnHiding(event -> onWindowClosing());

        getStage().setWidth(Options.getMainWindowWidth());
        getStage().setHeight(Options.getMainWindowHeight());

        /*
         * Application parameters:
         * --profile=<profile>
         */
        Application.Parameters params = MoneyApplication.application.getParameters();

        var profileName = params.getNamed().get("profile");
        if (profileName != null) {
            var profile = ConnectionProfileManager.get(profileName);
            if (profile == null) {
                LOGGER.warning("Profile " + profileName + " not found");
            } else {
                open(profile);
            }
        } else {
            if (ConnectionProfileManager.getAutoConnect()) {
                var profile = ConnectionProfileManager.getDefaultProfile();
                if (profile != null) {
                    open(profile);
                }
            }
        }
    }

    private void onProfiles() {
        new ConnectionProfilesEditor().showAndWait();
    }

    private void onManageCategories() {
        var controller = WindowManager.find(CategoryWindowController.class)
                .orElseGet(CategoryWindowController::new);

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onExit() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void onManageCurrencies() {
        var controller = WindowManager.find(CurrencyWindowController.class)
                .orElseGet(CurrencyWindowController::new);

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onManageAccounts() {
        var controller = WindowManager.find(AccountListWindowController.class)
                .orElseGet(AccountListWindowController::new);

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onManageContacts() {
        var controller = WindowManager.find(ContactListWindowController.class)
                .orElseGet(ContactListWindowController::new);

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    @Override
    public void onClose() {
        for (var clazz : WINDOW_CLASSES) {
            WindowManager.find(clazz).ifPresent(c -> ((BaseController) c).onClose());
        }

        tabPane.getSelectionModel().select(0);

        setTitle(AboutDialog.APP_TITLE);
        getDao().initialize(null);
        dbOpenProperty.set(false);
    }

    private void onOpenConnection() {
        new ConnectDialog().showAndWait()
                .ifPresent(this::open);
    }

    private void open(ConnectionProfile profile) {
        SshManager.setupTunnel(profile);
        DataSource ds = profile.buildDataSource();

        getDao().setEncryptionKey("");
        getDao().initialize(ds);

        Future loadResult = CompletableFuture
                .runAsync(() -> getDao().preload())
                .thenRun(() -> Platform.runLater(() -> {
                    setTitle(AboutDialog.APP_TITLE + " - " + profile.getName() + " - " + profile.getConnectionString());
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
        new OptionsDialog().showAndWait();
    }

    private void setTitle(String title) {
        getStage().setTitle(title);
    }

    private void onWindowClosing() {
        WINDOW_CLASSES.forEach(clazz -> WindowManager.find(clazz).ifPresent(c -> ((BaseController) c).onClose()));

        Options.setMainWindowWidth(getStage().widthProperty().doubleValue());
        Options.setMainWindowHeight(getStage().heightProperty().doubleValue());
    }

    private void xmlDump() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        var selected = fileChooser.showSaveDialog(null);

        if (selected != null) {
            CompletableFuture.runAsync(() -> {
                try (var outputStream = new FileOutputStream(selected)) {
                    new Export()
                            .withCategories(getDao().getCategories())
                            .withAccounts(getDao().getAccounts(), false)
                            .withCurrencies(getDao().getCurrencies())
                            .withContacts(getDao().getContacts())
                            .withTransactionGroups(getDao().getTransactionGroups())
                            .withTransactions(getDao().getTransactions(), false)
                            .doExport(outputStream);
                    Options.setLastExportDir(selected.getParent());
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        }
    }

    private void onImport() {
        new ImportWizard().showAndWait();
    }
}
