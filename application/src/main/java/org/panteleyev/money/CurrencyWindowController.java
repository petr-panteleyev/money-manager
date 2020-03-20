package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.Currency;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CurrencyWindowController extends BaseController {
    private final ObservableList<Currency> currencyList = FXCollections.observableArrayList();

    private final TableView<Currency> table = new TableView<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Currency> currencyListener = change ->
        Platform.runLater(() -> onCurrencyUpdate(change));

    CurrencyWindowController() {
        EventHandler<ActionEvent> addHandler = event -> onAddCurrency();
        EventHandler<ActionEvent> editHandler = event -> onEditCurrency();

        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)),
            createWindowMenu(RB),
            createHelpMenu(RB));

        // Context Menu
        table.setContextMenu(new ContextMenu(
            newMenuItem(RB, "menu.Edit.Add", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
            newTableColumn(RB, "column.Name", null, Currency::symbol, w.multiply(0.2)),
            newTableColumn(RB, "Description", null, Currency::description, w.multiply(0.8))
        ));

        var root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(table);

        currencyList.addAll(cache().currencies().values());
        table.setItems(currencyList);

        cache().currencies().addListener(new WeakMapChangeListener<>(currencyListener));
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
            new CurrencyDialog(this, selected).showAndWait()).ifPresent(c -> getDao().insertCurrency(c));
    }

    private void onCurrencyUpdate(MapChangeListener.Change<? extends UUID, ? extends Currency> change) {
        if (change.wasAdded()) {
            var currency = change.getValueAdded();

            // find if we have item with this id
            int index = currencyList.stream()
                .filter(c -> Objects.equals(c.uuid(), currency.uuid()))
                .findFirst()
                .map(currencyList::indexOf)
                .orElse(-1);

            if (index != -1) {
                currencyList.remove(index);
                currencyList.add(index, currency);
            } else {
                // simply add
                currencyList.add(currency);
            }

            table.getSelectionModel().select(currency);
        }
    }
}
