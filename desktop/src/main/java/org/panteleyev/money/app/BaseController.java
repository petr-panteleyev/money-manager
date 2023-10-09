/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
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
import org.panteleyev.money.app.document.DocumentWindowController;
import org.panteleyev.money.app.exchange.SecuritiesWindowController;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.MoneyRecord;

import java.util.Collection;
import java.util.function.Consumer;

import static org.controlsfx.control.action.ActionUtils.ACTION_SEPARATOR;
import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
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
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_U;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_F;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_SHIFT_P;
import static org.panteleyev.money.app.actions.ActionBuilder.actionBuilder;

public class BaseController extends Controller {
    static final WindowManager WINDOW_MANAGER = WindowManager.newInstance();

    protected final Action ACTION_CLOSE = new Action("Закрыть", event -> onClose());

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

    Menu createPortfolioMenu(BooleanProperty dbOpenProperty) {
        var securitiesMenuItem = menuItem("Ценные бумаги...",
                x -> getController(SecuritiesWindowController.class));

        if (dbOpenProperty != null) {
            securitiesMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        return newMenu("Портфель",
                securitiesMenuItem
        );
    }

    Menu createWindowMenu(BooleanProperty dbOpenProperty) {
        var transactionsMenuItem = menuItem("Проводки...", SHORTCUT_0,
                x -> getController(MainWindowController.class));
        var accountsMenuItem = menuItem("Счета...", SHORTCUT_1,
                x -> getController(AccountWindowController.class));
        var cardsMenuItem = menuItem("Карты...", SHORTCUT_2,
                x -> getController(CardWindowController.class));
        var statementMenuItem = menuItem("Выписки...", SHORTCUT_3,
                x -> getController(StatementWindowController.class));
        var requestsMenuItem = menuItem("Запросы...", SHORTCUT_4, x -> getRequestController());
        var chartsMenuItem = menuItem("Доходы и расходы...", SHORTCUT_5,
                x -> getController(IncomesAndExpensesWindowController.class));
        var currenciesMenuItem = menuItem("Валюты...", SHORTCUT_6,
                x -> getController(CurrencyWindowController.class));
        var categoriesMenuItem = menuItem("Категории...", SHORTCUT_7,
                x -> getController(CategoryWindowController.class));
        var contactsMenuItem = menuItem("Контакты...", SHORTCUT_8,
                x -> getController(ContactListWindowController.class));
        var documentsMenuItem = menuItem("Документы...", SHORTCUT_ALT_U,
                x -> getDocumentController(null));
        var periodicPaymentsMenuItem = menuItem("Периодические платежи...", SHORTCUT_SHIFT_P,
                x -> getController(PeriodicPaymentWindowController.class));

        if (dbOpenProperty != null) {
            accountsMenuItem.disableProperty().bind(dbOpenProperty.not());
            cardsMenuItem.disableProperty().bind(dbOpenProperty.not());
            statementMenuItem.disableProperty().bind(dbOpenProperty.not());
            requestsMenuItem.disableProperty().bind(dbOpenProperty.not());
            currenciesMenuItem.disableProperty().bind(dbOpenProperty.not());
            categoriesMenuItem.disableProperty().bind(dbOpenProperty.not());
            contactsMenuItem.disableProperty().bind(dbOpenProperty.not());
            chartsMenuItem.disableProperty().bind(dbOpenProperty.not());
            documentsMenuItem.disableProperty().bind(dbOpenProperty.not());
            periodicPaymentsMenuItem.disableProperty().bind(dbOpenProperty.not());
        }

        var menu = newMenu("Окно",
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
                contactsMenuItem,
                new SeparatorMenuItem(),
                documentsMenuItem,
                periodicPaymentsMenuItem
        );

        menu.setOnShowing(event -> {
            var lastIndex = menu.getItems().indexOf(periodicPaymentsMenuItem);
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
        return newMenu("Справка",
                menuItem("О программе", x -> new AboutDialog(this).showAndWait()));
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

    public static DocumentWindowController getDocumentController(MoneyRecord owner) {
        var controller = (DocumentWindowController) WINDOW_MANAGER
                .find(DocumentWindowController.class, c -> ((DocumentWindowController) c).thisOwner(owner))
                .orElseGet(() -> new DocumentWindowController(owner));

        var stage = controller.getStage();
        stage.show();
        stage.toFront();
        return controller;
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
