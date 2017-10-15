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
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Currency;
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
        EventHandler<ActionEvent> editHandler = event -> {
            Currency currency = table.getSelectionModel().getSelectedItem();
            if (currency != null) {
                openCurrencyDialog(currency);
            }
        };

        // Menu Bar
        MenuItem closeMenuItem = new MenuItem(RB.getString("menu.File.Close"));
        closeMenuItem.setOnAction(event -> onClose());

        Menu fileMenu = new Menu(RB.getString("menu.File"), null, closeMenuItem);

        MenuItem addMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        addMenuItem.setOnAction(addHandler);

        MenuItem editMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        editMenuItem.setOnAction(editHandler);

        Menu editMenu = new Menu(RB.getString("menu.Edit"), null, addMenuItem, editMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, createHelpMenu(RB));
        menuBar.setUseSystemMenuBar(true);

        // Context Menu
        MenuItem ctxAddMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);

        MenuItem ctxEditMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);

        table.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        TableColumn<Currency,String> colName = new TableColumn<>(RB.getString("column.Name"));
        TableColumn<Currency,String> colDescription = new TableColumn<>(RB.getString("column.Description"));

        table.getColumns().setAll(colName, colDescription);

        BorderPane root = new BorderPane();
        root.setPrefSize(600.0, 400.0);
        root.setTop(menuBar);
        root.setCenter(table);

        currencyList.addAll(getDao().currencies().values());
        table.setItems(currencyList);
        colName.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getSymbol()));
        colDescription.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        editMenuItem.disableProperty()
                .bind(table.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
                .bind(table.getSelectionModel().selectedItemProperty().isNull());

        getDao().currencies().addListener(new WeakMapChangeListener<>(currencyListener));

        setupWindow(root);
    }

    @Override
    public String getTitle() {
        return RB.getString("currency.Window.Title");
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
            Currency currency = change.getValueAdded();

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
