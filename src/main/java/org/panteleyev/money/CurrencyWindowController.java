/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Currency;
import java.util.Optional;
import static org.panteleyev.money.FXFactory.newMenuBar;
import static org.panteleyev.money.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CurrencyWindowController extends BaseController {
    private final ObservableList<Currency> currencyList = FXCollections.observableArrayList();

    private final TableView<Currency> table = new TableView<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<Integer, Currency> currencyListener = change ->
            Platform.runLater(() -> onCurrencyUpdate(change));

    CurrencyWindowController() {
        EventHandler<ActionEvent> addHandler = event -> openCurrencyDialog(null);
        EventHandler<ActionEvent> editHandler = event -> getSelectedCurrency().ifPresent(this::openCurrencyDialog);

        var disableBinding = table.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
                new Menu(RB.getString("menu.File"), null,
                        newMenuItem(RB, "menu.File.Close", event -> onClose())),
                new Menu(RB.getString("menu.Edit"), null,
                        newMenuItem(RB, "menu.Edit.Add", addHandler),
                        newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)),
                createHelpMenu(RB));

        // Context Menu
        table.setContextMenu(new ContextMenu(
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var colName = new TableColumn<Currency, String>(RB.getString("column.Name"));
        var colDescription = new TableColumn<Currency, String>(RB.getString("column.Description"));

        //noinspection unchecked
        table.getColumns().setAll(colName, colDescription);

        var root = new BorderPane();
        root.setPrefSize(600.0, 400.0);
        root.setTop(menuBar);
        root.setCenter(table);

        currencyList.addAll(getDao().currencies().values());
        table.setItems(currencyList);
        colName.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getSymbol()));
        colDescription.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        getDao().currencies().addListener(new WeakMapChangeListener<>(currencyListener));
        setupWindow(root);
    }

    @Override
    public String getTitle() {
        return RB.getString("currency.Window.Title");
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void openCurrencyDialog(Currency currency) {
        new CurrencyDialog(currency).showAndWait().ifPresent(c -> {
            if (c.getId() != 0) {
                getDao().updateCurrency(c);
            } else {
                getDao().insertCurrency(c.copy(getDao().generatePrimaryKey(Currency.class)));
            }
        });
    }

    private void onCurrencyUpdate(MapChangeListener.Change<? extends Integer, ? extends Currency> change) {
        if (change.wasAdded()) {
            var currency = change.getValueAdded();

            // find if we have item with this id
            int index = currencyList.stream()
                    .filter(c -> c.getId() == currency.getId())
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
