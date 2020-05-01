package org.panteleyev.money.app;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.model.Account;
import java.util.stream.Collectors;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.money.app.Constants.ELLIPSIS;
import static org.panteleyev.money.app.MainWindowController.CSS_PATH;
import static org.panteleyev.money.app.MainWindowController.RB;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.newInstance();

    BaseController() {
        super(CSS_PATH.toString());
    }

    protected BaseController(Stage stage, String css) {
        super(stage, css);
    }

    public void onClose() {
        Options.saveStageDimensions(getClass(), getStage());
        getStage().close();
    }

    Menu createWindowMenu() {
        return createWindowMenu(null);
    }

    Menu createWindowMenu(BooleanProperty dbOpenProperty) {
        var transactionsMenuItem = newMenuItem(RB, "Transactions", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN),
            x -> getController(MainWindowController.class));
        var accountsMenuItem = newMenuItem(RB, "Accounts", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN),
            x -> getController(AccountWindowController.class));
        var statementMenuItem = newMenuItem(RB, "Statements", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN),
            x -> getController(StatementWindowController.class));
        var requestsMenuItem = newMenuItem(RB, "Requests", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN),
            x -> getRequestController());
        var chartsMenuItem = newMenuItem(RB, "Incomes_and_Expenses", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN),
            x -> getController(IncomesAndExpensesWindowController.class));
        var currenciesMenuItem = newMenuItem(RB, "Currencies", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CurrencyWindowController.class));
        var categoriesMenuItem = newMenuItem(RB, "Categories", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CategoryWindowController.class));
        var contactsMenuItem = newMenuItem(RB, "Contacts", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN),
            x -> getController(ContactListWindowController.class));

        if (dbOpenProperty != null) {
            accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
            statementMenuItem.disableProperty().bind(dbOpenProperty.not());
            requestsMenuItem.disableProperty().bind(dbOpenProperty.not());
            currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
            categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
            contactsMenuItem.disableProperty().bind(dbOpenProperty.not());
            chartsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var menu = newMenu(RB, "Window",
            transactionsMenuItem,
            new SeparatorMenuItem(),
            accountsMenuItem,
            statementMenuItem,
            requestsMenuItem,
            chartsMenuItem,
            new SeparatorMenuItem(),
            currenciesMenuItem,
            categoriesMenuItem,
            contactsMenuItem);

        menu.setOnShowing(event -> {
            var lastIndex = menu.getItems().indexOf(contactsMenuItem);
            menu.getItems().remove(lastIndex + 1, menu.getItems().size());

            var accountControllers = WINDOW_MANAGER.getControllerStream(RequestWindowController.class)
                .filter(c -> ((RequestWindowController) c).getAccount() != null).collect(Collectors.toList());
            if (!accountControllers.isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
                accountControllers.forEach(c ->
                    menu.getItems().add(newMenuItem(c.getTitle(), x ->
                        c.getStage().toFront())));
            }
        });

        return menu;
    }

    protected Menu createHelpMenu() {
        return newMenu(RB, "menu.Help",
            newMenuItem(RB, "menu.Help.About", x -> new AboutDialog(this).showAndWait()));
    }

    static <T extends BaseController> void getController(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T controller = (T) WINDOW_MANAGER.find(clazz).orElseGet(() -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }

    private static void getRequestController() {
        getRequestController(null);
    }

    static void getRequestController(Account account) {
        var controller = (RequestWindowController) WINDOW_MANAGER
            .find(RequestWindowController.class, c -> ((RequestWindowController) c).thisAccount(account)).orElseGet(() -> {
                try {
                    return new RequestWindowController(account);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }
}
