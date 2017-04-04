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

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyDAO;
import java.net.URL;
import java.util.ResourceBundle;

public class CurrencyWindowController extends BaseController implements Initializable {
    private static final String FXML = "/org/panteleyev/money/CurrencyWindow.fxml";

    private final ObservableList<Currency> currencyList = FXCollections.observableArrayList();

    @FXML private Parent self;

    @FXML private TableView<Currency> table;
    @FXML private TableColumn<Currency,String> colName;
    @FXML private TableColumn<Currency,String> colDescription;

    @FXML private MenuBar  menuBar;
    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem ctxEditMenuItem;

    private ResourceBundle bundle;

    private final MapChangeListener<Integer,Currency> currencyListener =
            (MapChangeListener<Integer,Currency>)l -> Platform.runLater(this::updateWindow);

    CurrencyWindowController() {
        super(FXML, MainWindowController.UI_BUNDLE_PATH, true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;

        menuBar.setUseSystemMenuBar(true);

        currencyList.addAll(MoneyDAO.getInstance().currencyProperty().values());
        table.setItems(currencyList);
        colName.setCellValueFactory((TableColumn.CellDataFeatures<Currency, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getSymbol()));
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<Currency, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getDescription()));

        editMenuItem.disableProperty()
            .bind(table.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
            .bind(table.getSelectionModel().selectedItemProperty().isNull());

        MoneyDAO.getInstance().currencyProperty()
                .addListener(new WeakMapChangeListener<>(currencyListener));
    }

    @Override
    public String getTitle() {
        return bundle == null?
                "Currencies" : bundle.getString("currency.Window.Title");
    }

    public void onAddCurrency() {
        openCurrencyDialog(null);
    }

    public void onEditCurrency() {
        Currency currency = table.getSelectionModel().getSelectedItem();
        if (currency != null) {
            openCurrencyDialog(currency);
        }
    }

    private void openCurrencyDialog(Currency currency) {
        MoneyDAO dao = MoneyDAO.getInstance();
        new CurrencyDialog(currency).load().showAndWait().ifPresent(builder -> {
            if (builder.id().isPresent()) {
                dao.updateCurrency(builder.build());
            } else {
                dao.insertCurrency(builder.id(dao.generatePrimaryKey(Currency.class)).build());
            }
        });
    }

    private void updateWindow() {
        int selIndex = table.getSelectionModel().getSelectedIndex();
        currencyList.setAll(MoneyDAO.getInstance().currencyProperty().values());
        table.getSelectionModel().select(selIndex);
    }

    @Override
    protected Parent getSelf() {
        return self;
    }
}
