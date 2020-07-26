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
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CurrencyWindowController extends BaseController {
    private final TableView<Currency> table = new TableView<>(cache().getCurrencies());

    CurrencyWindowController() {
        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "Create", SHORTCUT_N,
                    event -> onAddCurrency()),
                newMenuItem(RB, "menu.Edit.Edit", SHORTCUT_E,
                    event -> onEditCurrency(), disableBinding)),
            createWindowMenu(),
            createHelpMenu());

        // Context Menu
        table.setContextMenu(new ContextMenu(
            newMenuItem(RB, "Create", event -> onAddCurrency()),
            newMenuItem(RB, "menu.Edit.Edit", event -> onEditCurrency(), disableBinding))
        );

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
            newTableColumn(RB, "column.Name", null, Currency::symbol, w.multiply(0.2)),
            newTableColumn(RB, "Description", null, Currency::description, w.multiply(0.8))
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
        new CurrencyDialog(this, null).showAndWait().ifPresent(c -> getDao().insertCurrency(c));
    }

    private void onEditCurrency() {
        getSelectedCurrency().flatMap(selected ->
            new CurrencyDialog(this, selected).showAndWait()).ifPresent(c -> getDao().updateCurrency(c));
    }
}
