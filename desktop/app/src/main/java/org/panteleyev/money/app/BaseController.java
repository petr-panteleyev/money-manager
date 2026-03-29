// Copyright © 2017-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.FxAction;
import org.panteleyev.fx.WindowManager;
import org.panteleyev.money.app.account.AccountWindowController;
import org.panteleyev.money.app.card.CardWindowController;
import org.panteleyev.money.app.category.CategoryWindowController;
import org.panteleyev.money.app.contact.ContactListWindowController;
import org.panteleyev.money.app.currency.CurrencyWindowController;
import org.panteleyev.money.app.exchange.SecuritiesWindowController;
import org.panteleyev.money.app.investment.InvestmentDealsWindowController;
import org.panteleyev.money.app.investment.InvestmentSummaryWindowController;
import org.panteleyev.money.model.Account;

import static org.panteleyev.fx.FxAction.fxAction;
import static org.panteleyev.fx.factories.MenuFactory.menu;
import static org.panteleyev.fx.factories.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_0;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_1;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_2;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_3;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_4;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_5;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_6;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_7;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_8;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_F;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.windowManager();

    protected final FxAction ACTION_CLOSE = fxAction("Закрыть").onAction(_ -> onClose());

    protected BaseController() {
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

    public Menu createWindowMenu() {
        return createWindowMenu(null);
    }

    protected Menu createPortfolioMenu() {
        return createPortfolioMenu(null);
    }

    protected Menu createPortfolioMenu(BooleanProperty dbOpenProperty) {
        var securitiesMenuItem = menuItem("Ценные бумаги...", _ -> getController(SecuritiesWindowController.class));
        if (dbOpenProperty != null) {
            securitiesMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var dealsMenuItem = menuItem("Инвестиционные сделки...",
                _ -> getController(InvestmentDealsWindowController.class));
        if (dbOpenProperty != null) {
            dealsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var investmentsMenuItem = menuItem("Инвестиции...",
                _ -> getController(InvestmentSummaryWindowController.class));
        if (dbOpenProperty != null) {
            investmentsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        return menu("Портфель",
                securitiesMenuItem,
                new SeparatorMenuItem(),
                dealsMenuItem,
                investmentsMenuItem);
    }

    Menu createWindowMenu(BooleanProperty dbOpenProperty) {
        var requestsMenuItem = menuItem("Запросы...", _ -> getRequestController());
        requestsMenuItem.setAccelerator(SHORTCUT_4);
        if (dbOpenProperty != null) {
            requestsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var contactsMenuItem = controllerMenuItem("Контакты...", SHORTCUT_8, ContactListWindowController.class,
                dbOpenProperty);

        var menu = menu("Окно",
                controllerMenuItem("Проводки...", SHORTCUT_0, MainWindowController.class, null),
                new SeparatorMenuItem(),
                controllerMenuItem("Счета...", SHORTCUT_1, AccountWindowController.class, dbOpenProperty),
                controllerMenuItem("Карты...", SHORTCUT_2, CardWindowController.class, dbOpenProperty),
                new SeparatorMenuItem(),
                controllerMenuItem("Выписки...", SHORTCUT_3, StatementWindowController.class, dbOpenProperty),
                requestsMenuItem,
                controllerMenuItem("Доходы и расходы...", SHORTCUT_5, IncomesAndExpensesWindowController.class,
                        dbOpenProperty),
                new SeparatorMenuItem(),
                controllerMenuItem("Валюты...", SHORTCUT_6, CurrencyWindowController.class, dbOpenProperty),
                controllerMenuItem("Категории...", SHORTCUT_7, CategoryWindowController.class, dbOpenProperty),
                contactsMenuItem
        );

        menu.setOnShowing(_ -> {
            var lastIndex = menu.getItems().indexOf(contactsMenuItem);
            menu.getItems().remove(lastIndex + 1, menu.getItems().size());

            var accountControllers = WINDOW_MANAGER.getControllerStream(RequestWindowController.class)
                    .filter(c -> ((RequestWindowController) c).getAccount() != null).toList();
            if (!accountControllers.isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
                accountControllers.forEach(c ->
                        menu.getItems().add(menuItem(c.getTitle(), _ -> c.getStage().toFront())));
            }
        });

        return menu;
    }

    private MenuItem controllerMenuItem(String text, KeyCodeCombination accelerator,
            Class<? extends BaseController> clazz, BooleanProperty dbOpenProperty)
    {
        var menuItem = menuItem(text, _ -> getController(clazz));
        menuItem.setAccelerator(accelerator);
        if (dbOpenProperty != null) {
            menuItem.disableProperty().bind(dbOpenProperty.not());
        }
        return menuItem;
    }

    protected Menu createHelpMenu() {
        return menu("Справка",
                menuItem("О программе", _ -> new AboutDialog(this).showAndWait()));
    }

    protected static <T extends BaseController> T getController(Class<T> clazz) {
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

    private static void getRequestController() {
        getRequestController(null);
    }

    protected static void getRequestController(Account account) {
        var controller = (RequestWindowController) WINDOW_MANAGER
                .find(RequestWindowController.class, c -> ((RequestWindowController) c).thisAccount(account)).orElseGet(
                        () -> {
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

    // Actions
    protected static FxAction searchAction(EventHandler<ActionEvent> handler) {
        return fxAction("Поиск").onAction(handler)
                .accelerator(SHORTCUT_F);
    }
}
