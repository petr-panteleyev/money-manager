package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
import java.util.ResourceBundle;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.money.Constants.ELLIPSIS;
import static org.panteleyev.money.MainWindowController.CSS_PATH;

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

    Menu createWindowMenu(ResourceBundle rb) {
        return createWindowMenu(rb, null);
    }

    Menu createWindowMenu(ResourceBundle rb, BooleanProperty dbOpenProperty) {
        var transactionsMenuItem = newMenuItem(rb, "Transactions", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN),
            x -> getController(MainWindowController.class));
        var accountsMenuItem = newMenuItem(rb, "Accounts", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN),
            x -> getController(AccountWindowController.class));
        var statementMenuItem = newMenuItem(rb, "Statements", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN),
            x -> getController(StatementWindowController.class));
        var requestsMenuItem = newMenuItem(rb, "Requests", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN),
            x -> getController(RequestWindowController.class));
        var chartsMenuItem = newMenuItem(rb, "Incomes_and_Expenses", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN),
            x -> getController(IncomesAndExpensesWindowController.class));
        var currenciesMenuItem = newMenuItem(rb, "Currencies", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CurrencyWindowController.class));
        var categoriesMenuItem = newMenuItem(rb, "Categories", ELLIPSIS,
            new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CategoryWindowController.class));
        var contactsMenuItem = newMenuItem(rb, "Contacts", ELLIPSIS,
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

        return newMenu(rb, "Window",
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
    }

    protected Menu createHelpMenu(ResourceBundle rb) {
        return newMenu(rb, "menu.Help",
            newMenuItem(rb, "menu.Help.About", x -> new AboutDialog(this).showAndWait()));
    }

    static <T extends BaseController> T getController(Class<T> clazz) {
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

        return controller;
    }
}
