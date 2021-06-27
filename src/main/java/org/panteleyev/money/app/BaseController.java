/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.app.options.Options;
import org.panteleyev.money.model.Account;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_0;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_1;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_2;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_3;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_4;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_5;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_6;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_7;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.bundles.Internationalization.I18M_MISC_INCOMES_AND_EXPENSES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_HELP;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ABOUT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_WINDOW;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACCOUNTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CATEGORIES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONTACTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCIES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REQUESTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TRANSACTIONS;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.newInstance();

    BaseController() {
        super(options().getMainCssFilePath());
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
        var transactionsMenuItem = menuItem(fxString(UI, I18N_WORD_TRANSACTIONS, ELLIPSIS), SHORTCUT_0,
            x -> getController(MainWindowController.class));
        var accountsMenuItem = menuItem(fxString(UI, I18N_WORD_ACCOUNTS, ELLIPSIS), SHORTCUT_1,
            x -> getController(AccountWindowController.class));
        var statementMenuItem = menuItem(fxString(UI, I18N_WORD_STATEMENTS, ELLIPSIS), SHORTCUT_2,
            x -> getController(StatementWindowController.class));
        var requestsMenuItem = menuItem(fxString(UI, I18N_WORD_REQUESTS, ELLIPSIS), SHORTCUT_3,
            x -> getRequestController());
        var chartsMenuItem = menuItem(fxString(UI, I18M_MISC_INCOMES_AND_EXPENSES, ELLIPSIS), SHORTCUT_4,
            x -> getController(IncomesAndExpensesWindowController.class));
        var currenciesMenuItem = menuItem(fxString(UI, I18N_WORD_CURRENCIES, ELLIPSIS), SHORTCUT_5,
            x -> getController(CurrencyWindowController.class));
        var categoriesMenuItem = menuItem(fxString(UI, I18N_WORD_CATEGORIES, ELLIPSIS), SHORTCUT_6,
            x -> getController(CategoryWindowController.class));
        var contactsMenuItem = menuItem(fxString(UI, I18N_WORD_CONTACTS, ELLIPSIS), SHORTCUT_7,
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

        var menu = newMenu(fxString(UI, I18N_MENU_WINDOW),
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
                .filter(c -> ((RequestWindowController) c).getAccount() != null).toList();
            if (!accountControllers.isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
                accountControllers.forEach(c ->
                    menu.getItems().add(menuItem(c.getTitle(), x ->
                        c.getStage().toFront())));
            }
        });

        return menu;
    }

    protected Menu createHelpMenu() {
        return newMenu(fxString(UI, I18N_MENU_HELP),
            menuItem(fxString(UI, I18N_MENU_ITEM_ABOUT), x -> new AboutDialog(this).showAndWait()));
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
