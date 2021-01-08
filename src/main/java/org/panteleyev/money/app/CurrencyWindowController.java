/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.options.Options;
import org.panteleyev.money.model.Currency;
import java.util.List;
import java.util.Optional;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CurrencyWindowController extends BaseController {
    private final TableView<Currency> table = new TableView<>(cache().getCurrencies());

    CurrencyWindowController() {
        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = menuBar(
            newMenu(fxString(RB, "File"),
                menuItem(fxString(RB, "Close"), event -> onClose())),
            newMenu(fxString(RB, "menu.Edit"),
                menuItem(fxString(RB, "Create"), SHORTCUT_N,
                    event -> onAddCurrency()),
                menuItem(fxString(RB, "menu.Edit.Edit"), SHORTCUT_E,
                    event -> onEditCurrency(), disableBinding)),
            createWindowMenu(),
            createHelpMenu());

        // Context Menu
        table.setContextMenu(new ContextMenu(
            menuItem(fxString(RB, "Create"), event -> onAddCurrency()),
            menuItem(fxString(RB, "menu.Edit.Edit"), event -> onEditCurrency(), disableBinding))
        );

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
            tableColumn(fxString(RB, "column.Name"), b ->
                b.withPropertyCallback(Currency::symbol).withWidthBinding(w.multiply(0.2))),
            tableColumn(fxString(RB, "Description"), b ->
                b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));

        var root = new BorderPane(table, menuBar, null, null, null);
        setupWindow(root);
        Options.loadStageDimensions(getClass(), getStage());
    }

    @Override
    public String getTitle() {
        return RB.getString("Currencies");
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void onAddCurrency() {
        new CurrencyDialog(this, options().getDialogCssFileUrl(), null)
            .showAndWait()
            .ifPresent(c -> getDao().insertCurrency(c));
    }

    private void onEditCurrency() {
        getSelectedCurrency()
            .flatMap(selected ->
                new CurrencyDialog(this, options().getDialogCssFileUrl(), selected).showAndWait())
            .ifPresent(c -> getDao().updateCurrency(c));
    }
}
