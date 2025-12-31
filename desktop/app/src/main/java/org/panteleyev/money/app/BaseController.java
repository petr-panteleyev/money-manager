// Copyright © 2017-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;
import org.panteleyev.fx.Controller;
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

import java.util.Collection;
import java.util.function.Consumer;

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.functional.Scope.apply;
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
import static org.panteleyev.money.app.actions.ActionBuilder.actionBuilder;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.windowManager();

    protected final Action ACTION_CLOSE = new Action("Закрыть", _ -> onClose());

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
        return menu("Портфель",
                apply(menuItem("Ценные бумаги..."), menuItem -> {
                    menuItem.setOnAction(_ -> getController(SecuritiesWindowController.class));
                    if (dbOpenProperty != null) {
                        menuItem.disableProperty().bind(dbOpenProperty.not());
                    }
                }),
                new SeparatorMenuItem(),
                apply(menuItem("Инвестиционные сделки..."), menuItem -> {
                    menuItem.setOnAction(_ -> getController(InvestmentDealsWindowController.class));
                    if (dbOpenProperty != null) {
                        menuItem.disableProperty().bind(dbOpenProperty.not());
                    }
                }),
                apply(menuItem("Инвестиции..."), menuItem -> {
                    menuItem.setOnAction(_ -> getController(InvestmentSummaryWindowController.class));
                    if (dbOpenProperty != null) {
                        menuItem.disableProperty().bind(dbOpenProperty.not());
                    }
                })
        );
    }

    Menu createWindowMenu(BooleanProperty dbOpenProperty) {
        var transactionsMenuItem = apply(menuItem("Проводки..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_0);
            menuItem.setOnAction(_ -> getController(MainWindowController.class));
        });
        var accountsMenuItem = apply(menuItem("Счета..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_1);
            menuItem.setOnAction(_ -> getController(AccountWindowController.class));
        });
        var cardsMenuItem = apply(menuItem("Карты..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_2);
            menuItem.setOnAction(_ -> getController(CardWindowController.class));
        });
        var statementMenuItem = apply(menuItem("Выписки..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_3);
            menuItem.setOnAction(_ -> getController(StatementWindowController.class));
        });
        var requestsMenuItem = apply(menuItem("Запросы..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_4);
            menuItem.setOnAction(_ -> getRequestController());
        });
        var chartsMenuItem = apply(menuItem("Доходы и расходы..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_5);
            menuItem.setOnAction(_ -> getController(IncomesAndExpensesWindowController.class));
        });
        var currenciesMenuItem = apply(menuItem("Валюты..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_6);
            menuItem.setOnAction(_ -> getController(CurrencyWindowController.class));
        });
        var categoriesMenuItem = apply(menuItem("Категории..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_7);
            menuItem.setOnAction(_ -> getController(CategoryWindowController.class));
        });
        var contactsMenuItem = apply(menuItem("Контакты..."), menuItem -> {
            menuItem.setAccelerator(SHORTCUT_8);
            menuItem.setOnAction(_ -> getController(ContactListWindowController.class));
        });

        if (dbOpenProperty != null) {
            accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
            cardsMenuItem.disableProperty().bind(dbOpenProperty.not());
            statementMenuItem.disableProperty().bind(dbOpenProperty.not());
            requestsMenuItem.disableProperty().bind(dbOpenProperty.not());
            currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
            categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
            contactsMenuItem.disableProperty().bind(dbOpenProperty.not());
            chartsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var menu = menu("Окно",
                transactionsMenuItem,
                new SeparatorMenuItem(),
                accountsMenuItem,
                cardsMenuItem,
                new SeparatorMenuItem(),
                statementMenuItem,
                requestsMenuItem,
                chartsMenuItem,
                new SeparatorMenuItem(),
                currenciesMenuItem,
                categoriesMenuItem,
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
    protected static Action searchAction(Consumer<ActionEvent> handler) {
        return actionBuilder("Поиск", handler)
                .accelerator(SHORTCUT_F)
                .build();
    }

    static Menu createMenu(String text, Collection<Action> actions) {
        var menu = new Menu(text);
        for (var action : actions) {
            if (action == ACTION_SEPARATOR) {
                menu.getItems().add(new SeparatorMenuItem());
            } else {
                menu.getItems().add(createMenuItem(action));
            }
        }
        return menu;
    }
}
