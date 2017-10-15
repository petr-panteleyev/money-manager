/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.profiles.ConnectDialog;
import org.panteleyev.money.profiles.ConnectionProfile;
import org.panteleyev.money.profiles.ConnectionProfileManager;
import org.panteleyev.money.profiles.ConnectionProfilesEditor;
import org.panteleyev.money.statements.StatementTab;
import org.panteleyev.money.xml.Export;
import org.panteleyev.utilities.fx.Controller;
import org.panteleyev.utilities.fx.WindowManager;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class MainWindowController extends BaseController {
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

    private static final List<Class<? extends Controller>> WINDOW_CLASSES = Arrays.asList(
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
        MenuItem m2 = new MenuItem(RB.getString("menu.File.Connect"));
        m2.setOnAction(event -> onOpenConnection());
        MenuItem m3 = new MenuItem(RB.getString("menu.File.Close"));
        m3.setOnAction(event -> onClose());
        MenuItem m4 = new MenuItem(RB.getString("menu.File.Exit"));
        m4.setOnAction(event -> onExit());

        Menu fileMenu = new Menu(RB.getString("menu.File"), null,
                m2, new SeparatorMenuItem(), m3, new SeparatorMenuItem(), m4);

        MenuItem m5 = new MenuItem(RB.getString("menu.Edit.Delete"));

        MenuItem currenciesMenuItem = new MenuItem(RB.getString("menu.Edit.Currencies"));
        currenciesMenuItem.setOnAction(event -> onManageCurrencies());
        MenuItem categoriesMenuItem = new MenuItem(RB.getString("menu.Edit.Categories"));
        categoriesMenuItem.setOnAction(event -> onManageCategories());
        MenuItem accountsMenuItem = new MenuItem(RB.getString("menu.Edit.Accounts"));
        accountsMenuItem.setOnAction(event -> onManageAccounts());
        MenuItem contactsMenuItem = new MenuItem(RB.getString("menu.Edit.Contacts"));
        contactsMenuItem.setOnAction(event -> onManageContacts());

        Menu editMenu = new Menu(RB.getString("menu.Edit"), null,
                m5, new SeparatorMenuItem(),
                currenciesMenuItem, categoriesMenuItem, accountsMenuItem, contactsMenuItem);

        MenuItem dumpXmlMenuItem = new MenuItem(RB.getString("menu.Tools.Export"));
        dumpXmlMenuItem.setOnAction(event -> xmlDump());

        MenuItem importMenuItem = new MenuItem(RB.getString("word.Import") + "...");
        importMenuItem.setOnAction(event -> onImport());

        MenuItem profilesMenuItem = new MenuItem(RB.getString("menu.Tools.Profiles"));
        profilesMenuItem.setOnAction(event -> onProfiles());

        MenuItem m7 = new MenuItem(RB.getString("menu.Tools.Options"));
        m7.setOnAction(event -> onOptions());

        Menu toolsMenu = new Menu(RB.getString("menu.Tools"), null,
                dumpXmlMenuItem,
                importMenuItem,
                new SeparatorMenuItem(),
                profilesMenuItem,
                new SeparatorMenuItem(),
                m7
        );

        /* Dummy menu item is required in order to let onShowing() fire up first time */
        windowMenu.getItems().setAll(new MenuItem("dummy"));

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, toolsMenu,
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

        Tab t2 = new Tab(RB.getString("tab.Transactions"), transactionTab);
        t2.disableProperty().bind(dbOpenProperty.not());
        t2.selectedProperty().addListener((x, y, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> transactionTab.getTransactionEditor().clear());
                Platform.runLater(() -> transactionTab.scrollToEnd());
            }
        });

        Tab t3 = new Tab(RB.getString("tab.Requests"), requestTab);
        t3.disableProperty().bind(dbOpenProperty.not());

        Tab t4 = new Tab(RB.getString("Statement"), statementsTab);
        t4.disableProperty().bind(dbOpenProperty.not());

        tabPane.getTabs().addAll(
                new Tab(RB.getString("tab.Accouts"), accountsTab),
                t2, t3, t4
        );

        windowMenu.setOnShowing(event -> {
            windowMenu.getItems().clear();

            windowMenu.getItems().add(new MenuItem("Money Manager"));
            windowMenu.getItems().add(new SeparatorMenuItem());

            WindowManager.getFrames().forEach(frame -> {
                MenuItem item = new MenuItem(frame.getTitle());
                item.setOnAction(action -> frame.getStage().toFront());
                windowMenu.getItems().add(item);
            });
        });

        getStage().setOnHiding(event -> onWindowClosing());

        getStage().setWidth(Options.getMainWindowWidth());
        getStage().setHeight(Options.getMainWindowHeight());

        /*
         * Application parameters:
         * --profile="<profile>"
         *     or
         * --host=<host>
         * --port=<port>
         * --user=<user>
         * --password=<password>
         * --name=<name>
         */
        Application.Parameters params = MoneyApplication.application.getParameters();

        String profileName = params.getNamed().get("profile");
        if (profileName != null) {
            ConnectionProfile profile = ConnectionProfileManager.get(profileName);
            if (profile == null) {
                Logging.getLogger().warning("Profile $profileName not found");
            } else {
                open(profile);
            }
        } else {
            String name = params.getNamed().get("name");
            if (name != null) {
                // check mandatory parameters
                String host = params.getNamed().getOrDefault("host", "localhost");
                int port = Integer.parseInt(params.getNamed().getOrDefault("port", "3306"));
                String user = params.getNamed().get("user");
                String password = params.getNamed().getOrDefault("password", "");

                if (user == null) {
                    throw new IllegalArgumentException("User name cannot be empty");
                }

                ConnectionProfile profile = new ConnectionProfile("", host, port, user, password, name);
                open(profile);
            } else {
                if (ConnectionProfileManager.getAutoConnect()) {
                    ConnectionProfile profile = ConnectionProfileManager.getDefaultProfile();
                    if (profile != null) {
                        open(profile);
                    }
                }
            }
        }
    }

    private void onProfiles() {
        new ConnectionProfilesEditor().showAndWait();
    }

    private void onManageCategories() {
        Controller controller = WindowManager.find(CategoryWindowController.class)
                .orElseGet(CategoryWindowController::new);

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onExit() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void onManageCurrencies() {
        Controller controller = WindowManager.find(CurrencyWindowController.class)
                .orElseGet(CurrencyWindowController::new);

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onManageAccounts() {
        Controller controller = WindowManager.find(AccountListWindowController.class)
                .orElseGet(AccountListWindowController::new);

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private void onManageContacts() {
        Controller controller = WindowManager.find(ContactListWindowController.class)
                .orElseGet(ContactListWindowController::new);

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    @Override
    public void onClose() {
        for (Class<? extends Controller> clazz : WINDOW_CLASSES) {
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

    private void open(ConnectionProfile builder) {
        DataSource ds = builder.build();

        getDao().initialize(ds);

        Future loadResult = CompletableFuture
                .runAsync(() -> getDao().preload())
                .thenRun(() -> Platform.runLater(() -> {
                    setTitle(AboutDialog.APP_TITLE + " - " + builder.getConnectionString());
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to file");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("XML Files", "*.xml"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));

        File selected = fileChooser.showSaveDialog(null);

        if (selected != null) {
            CompletableFuture.runAsync(() -> {
                try (OutputStream outputStream = new FileOutputStream(selected)) {
                    new Export()
                            .withCategories(getDao().getCategories())
                            .withAccounts(getDao().getAccounts(), false)
                            .withCurrencies(getDao().getCurrencies())
                            .withContacts(getDao().getContacts())
                            .withTransactionGroups(getDao().getTransactionGroups())
                            .withTransactions(getDao().getTransactions(), false)
                            .doExport(outputStream);

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
