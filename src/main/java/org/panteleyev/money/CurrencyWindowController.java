/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.MoneyDAO;
import java.util.ResourceBundle;

public class CurrencyWindowController extends BaseController {
    private final ObservableList<Currency> currencyList = FXCollections.observableArrayList();

    private final ResourceBundle rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final BorderPane root = new BorderPane();

    private final TableView<Currency> table = new TableView<>();

    private final MapChangeListener<Integer, Currency> currencyListener = this::onCurrencyUpdate;

    CurrencyWindowController() {
        super(null);
        initialize();
        setupWindow(root);
    }

    private void initialize() {
        EventHandler<ActionEvent> addHandler = (evt) -> openCurrencyDialog(null);
        EventHandler<ActionEvent> editHandler = (evt) -> {
            Currency currency = table.getSelectionModel().getSelectedItem();
            if (currency != null) {
                openCurrencyDialog(currency);
            }
        };

        // Menu Bar
        MenuItem closeMenuItem = new MenuItem(rb.getString("menu.File.Close"));
        closeMenuItem.setOnAction(ACTION_FILE_CLOSE);
        Menu fileMenu = new Menu(rb.getString("menu.File"), null, closeMenuItem);

        MenuItem addMenuItem = new MenuItem(rb.getString("menu.Edit.Add"));
        addMenuItem.setOnAction(addHandler);
        MenuItem editMenuItem = new MenuItem(rb.getString("menu.Edit.Edit"));
        editMenuItem.setOnAction(editHandler);
        Menu editMenu = new Menu(rb.getString("menu.Edit"), null, addMenuItem, editMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, createHelpMenu(rb));
        menuBar.setUseSystemMenuBar(true);

        // Context Menu
        MenuItem ctxAddMenuItem = new MenuItem(rb.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);
        MenuItem ctxEditMenuItem = new MenuItem(rb.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);
        table.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        TableColumn<Currency,String> colName = new TableColumn<>(rb.getString("column.Name"));
        TableColumn<Currency,String> colDescription = new TableColumn<>(rb.getString("column.Description"));

        table.getColumns().setAll(colName, colDescription);

        root.setPrefSize(600, 400);
        root.setTop(menuBar);
        root.setCenter(table);

        currencyList.addAll(MoneyDAO.getInstance().currencies().values());
        table.setItems(currencyList);
        colName.setCellValueFactory((TableColumn.CellDataFeatures<Currency, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getSymbol()));
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<Currency, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        editMenuItem.disableProperty()
            .bind(table.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
            .bind(table.getSelectionModel().selectedItemProperty().isNull());

        MoneyDAO.getInstance().currencies()
                .addListener(new WeakMapChangeListener<>(currencyListener));
    }

    @Override
    public String getTitle() {
        return rb.getString("currency.Window.Title");
    }

    private void openCurrencyDialog(Currency currency) {
        MoneyDAO dao = MoneyDAO.getInstance();
        new CurrencyDialog(currency).showAndWait().ifPresent(builder -> {
            if (builder.id() != 0) {
                dao.updateCurrency(builder.build());
            } else {
                dao.insertCurrency(builder.id(dao.generatePrimaryKey(Currency.class)).build());
            }
        });
    }

    private void onCurrencyUpdate(MapChangeListener.Change<? extends Integer, ? extends Currency> change) {
        if (change.wasAdded()) {
            Currency currency = change.getValueAdded();

            // find if we have item with this id
            int index = currencyList.stream()
                    .filter(c -> c.getId() == currency.getId())
                    .findAny()
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
