/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.MoneyRecord;

import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_0;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_1;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_2;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_3;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_4;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_5;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_6;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_7;
import static org.panteleyev.money.bundles.Internationalization.I18M_MISC_INCOMES_AND_EXPENSES;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_HELP;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ABOUT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_WINDOW;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACCOUNTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CATEGORIES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONTACTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCIES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DOCUMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_REQUESTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_STATEMENTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TRANSACTIONS;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.newInstance();

    BaseController() {
        super(settings().getMainCssFilePath());
    }

    protected BaseController(Stage stage, String css) {
        super(stage, css);
    }

    public void onClose() {
        getStage().close();
    }

    @Override
    protected void onWindowHiding() {
        settings().saveStageDimensions(this);
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
        var documentsMenuItem = menuItem(fxString(UI, I18N_WORD_DOCUMENTS, ELLIPSIS),
                x -> getDocumentController(null));

        if (dbOpenProperty != null) {
            accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
            statementMenuItem.disableProperty().bind(dbOpenProperty.not());
            requestsMenuItem.disableProperty().bind(dbOpenProperty.not());
            currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
            categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
            contactsMenuItem.disableProperty().bind(dbOpenProperty.not());
            chartsMenuItem.disableProperty().bind(dbOpenProperty.not());
            documentsMenuItem.disableProperty().bind(dbOpenProperty.not());
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
                contactsMenuItem,
                new SeparatorMenuItem(),
                documentsMenuItem
        );

        menu.setOnShowing(event -> {
            var lastIndex = menu.getItems().indexOf(documentsMenuItem);
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

    static void getDocumentController(MoneyRecord owner) {
        var controller = (DocumentWindowController) WINDOW_MANAGER
                .find(DocumentWindowController.class, c -> ((DocumentWindowController) c).thisOwner(owner))
                .orElseGet(() -> new DocumentWindowController(owner));

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
    }
}
