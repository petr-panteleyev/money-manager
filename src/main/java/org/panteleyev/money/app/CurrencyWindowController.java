/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.Currency;
import java.util.List;
import java.util.Optional;
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCIES;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_DESCRIPTION;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;

final class CurrencyWindowController extends BaseController {
    private final TableView<Currency> table = new TableView<>(cache().getCurrencies());

    CurrencyWindowController() {
        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = menuBar(
            newMenu(fxString(UI, I18N_MENU_FILE),
                menuItem(fxString(UI, I18N_WORD_CLOSE), event -> onClose())),
            newMenu(fxString(UI, I18N_MENU_EDIT),
                menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N,
                    event -> onAddCurrency()),
                menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E,
                    event -> onEditCurrency(), disableBinding)),
            createWindowMenu(),
            createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context Menu
        table.setContextMenu(new ContextMenu(
            menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), event -> onAddCurrency()),
            menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), event -> onEditCurrency(), disableBinding))
        );

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
            tableColumn(fxString(UI, I18N_WORD_ENTITY_NAME), b ->
                b.withPropertyCallback(Currency::symbol).withWidthBinding(w.multiply(0.2))),
            tableColumn(fxString(UI, I18N_WORD_DESCRIPTION), b ->
                b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));

        var root = new BorderPane(table, menuBar, null, null, null);
        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return UI.getString(I18N_WORD_CURRENCIES);
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void onAddCurrency() {
        new CurrencyDialog(this, settings().getDialogCssFileUrl(), null)
            .showAndWait()
            .ifPresent(c -> dao().insertCurrency(c));
    }

    private void onEditCurrency() {
        getSelectedCurrency()
            .flatMap(selected ->
                new CurrencyDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
            .ifPresent(c -> dao().updateCurrency(c));
    }
}
