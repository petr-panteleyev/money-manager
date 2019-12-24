/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.panteleyev.commons.fx.Controller;
import org.panteleyev.commons.fx.WindowManager;
import java.util.ResourceBundle;
import static org.panteleyev.commons.fx.FXFactory.newMenu;
import static org.panteleyev.commons.fx.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.CSS_PATH;

public class BaseController extends Controller {
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
        var transactionsMenuItem = newMenuItem(rb, "menu.window.transactions",
            new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN),
            x -> getController(MainWindowController.class));
        var accountsMenuItem = newMenuItem(rb, "menu.window.accounts",
            new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN),
            x -> getController(AccountWindowController.class));
        var statementMenuItem = newMenuItem(rb, "menu.window.statement",
            new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN),
            x -> getController(StatementWindowController.class));
        var requestsMenuItem = newMenuItem(rb, "menu.window.requests",
            new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN),
            x -> getController(RequestWindowController.class));
        var chartsMenuItem = newMenuItem(rb, "menu.window.charts",
            new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN),
            x -> getController(ChartsWindowController.class));
        var currenciesMenuItem = newMenuItem(rb, "menu.Edit.Currencies",
            new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CurrencyWindowController.class));
        var categoriesMenuItem = newMenuItem(rb, "menu.Edit.Categories",
            new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN),
            x -> getController(CategoryWindowController.class));
        var contactsMenuItem = newMenuItem(rb, "menu.Edit.Contacts",
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

        return newMenu(rb, "menu.Window",
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
            newMenuItem(rb, "menu.Help.About", x -> new AboutDialog().showAndWait()));
    }

    static <T extends BaseController> T getController(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T controller = (T) WindowManager.find(clazz).orElseGet(() -> {
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
