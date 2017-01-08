/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.sql.DataSource;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.utilities.fx.Controller;
import org.panteleyev.utilities.fx.WindowManager;

public class MainWindowController extends Controller implements Initializable {
    public static final String UI_BUNDLE_PATH = "org.panteleyev.money.ui";

    @FXML private BorderPane    self;
    @FXML private TabPane       tabPane;

    @FXML private Label         progressLabel;
    @FXML private ProgressBar   progressBar;

    @FXML private Menu          windowMenu;

    @FXML private MenuItem      currenciesMenuItem;
    @FXML private MenuItem      categoriesMenuItem;
    @FXML private MenuItem      accountsMenuItem;
    @FXML private MenuItem      contactsMenuItem;

    private static final Collection<Class<? extends Controller>> WINDOW_CLASSES =
            Arrays.asList(
                    ContactListWindowController.class,
                    AccountListWindowController.class,
                    CategoryWindowController.class,
                    CurrencyWindowController.class
            );

    private final AccountsTab accountsTab = new AccountsTab();
    private final TransactionsTab transactionTab = (TransactionsTab)new TransactionsTab().load();
    private final RequestTab requestTab = new RequestTab();

    private final BooleanProperty dbOpenProperty = new SimpleBooleanProperty(false);

    public static final Validator<String> BIG_DECIMAL_VALIDATOR = (Control control, String value) -> {
        boolean invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }
        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    public MainWindowController() {
        super("/org/panteleyev/money/MainWindow.fxml", UI_BUNDLE_PATH, true);
    }

    public BooleanProperty dbOpenProperty() {
        return dbOpenProperty;
    }

    @Override
    public String getTitle() {
        return "Money Manager";
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        progressLabel.setVisible(false);
        progressBar.setVisible(false);

        currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
        categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
        accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
        contactsMenuItem.disableProperty().bind(dbOpenProperty.not());

        Tab t2 = new Tab(rb.getString("tab.Transactions"), transactionTab.getPane());
        t2.disableProperty().bind(dbOpenProperty.not());

        Tab t3 = new Tab(rb.getString("tab.Requests"), requestTab);
        t3.disableProperty().bind(dbOpenProperty.not());

        tabPane.getTabs().addAll(
            new Tab(rb.getString("tab.Accouts"), accountsTab),
            t2, t3
        );

        windowMenu.setOnShowing(e -> {
            windowMenu.getItems().clear();

            windowMenu.getItems().add(new MenuItem("Money Manager"));
            windowMenu.getItems().add(new SeparatorMenuItem());

            WindowManager.getFrames().forEach(c -> {
                MenuItem item = new MenuItem(c.getTitle());
                item.setOnAction(a -> c.getStage().toFront());
                windowMenu.getItems().add(item);
            });
        });

        File file;

        Application.Parameters params = MoneyApplication.getApplication().getParameters();
        String fileName = params.getNamed().get("file");
        if (fileName != null && !fileName.isEmpty()) {
            file = new File(fileName);
        } else {
            file = Options.getDbFile();
        }

        if (file != null) {
            if (file.exists()) {
                open(file);
            } else {
                Options.setDbFile(null);
            }
        }
    }

    public void onManageCategories() {
        Controller controller = WindowManager.find(CategoryWindowController.class)
                .orElseGet(() -> new CategoryWindowController().load());

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    public void onExit() {
        System.exit(0);
    }

    public void onManageCurrencies() {
        Controller controller = WindowManager.find(CurrencyWindowController.class)
                .orElseGet(() -> new CurrencyWindowController().load());

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    public void onManageAccounts() {
        Controller controller = WindowManager.find(AccountListWindowController.class)
                .orElseGet(() -> new AccountListWindowController().load());

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    public void onManageContacts() {
        Controller controller = WindowManager.find(ContactListWindowController.class)
                .orElseGet(() -> new ContactListWindowController().load());

        Stage stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    public void onNew() {
        FileChooser d = new FileChooser();
        d.setTitle("New Database File");
        d.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SQLite Database", "*.db")
        );
        File file = d.showSaveDialog(null);
        newFile(file);
    }

    private void newFile(File file) {
        Options.setDbFile(file);

        if (file != null) {
            DataSource ds = new MoneyDAO.Builder()
                .file(file.getAbsolutePath())
                .build();

            CompletableFuture.runAsync(() -> {
                MoneyDAO dao = MoneyDAO.initialize(ds);
                dao.createTables();
                dao.setupNewDatabase();
                dao.preload();
            }).thenRun(() -> dbOpenProperty.set(true));
        }
    }

    public void onClose() {
        WINDOW_CLASSES.forEach(clazz ->
                WindowManager.find(clazz).ifPresent(c -> ((BaseController)c).onClose()));

        tabPane.getSelectionModel().select(0);

        MoneyDAO.initialize(null);
        Options.setDbFile(null);
        dbOpenProperty.set(false);
    }

    public void onOpen() {
        FileChooser d = new FileChooser();
        d.setTitle("Database File");
        d.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("SQLite Database", "*.db")
        );
        File file = d.showOpenDialog(null);
        open(file);
    }

    private void open(File file) {
        Options.setDbFile(file);

        if (file != null) {
            DataSource ds = new MoneyDAO.Builder()
                .file(file.getAbsolutePath())
                .build();
            MoneyDAO.initialize(ds);

            CompletableFuture
                    .runAsync(() -> MoneyDAO.getInstance().preload())
                    .thenRun(() -> dbOpenProperty.set(true));
        }
    }

    public void onExport() {

    }

    public void onOptions() {
        OptionsDialog d = new OptionsDialog();
        d.showAndWait();
    }
}
