/*
 * Copyright (c) 2017, 2020, Petr Panteleyev <petr@panteleyev.org>
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
            newTableColumn(RB, "column.Name", null, Currency::getSymbol, w.multiply(0.2)),
            newTableColumn(RB, "Description", null, Currency::getDescription, w.multiply(0.8))
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
                .filter(c -> Objects.equals(c.getUuid(), currency.getUuid()))
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
