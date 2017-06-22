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

package org.panteleyev.money

import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.WeakMapChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import org.panteleyev.money.persistence.Currency
import org.panteleyev.money.persistence.MoneyDAO
import java.util.ResourceBundle

class CurrencyWindowController : BaseController() {
    private val currencyList = FXCollections.observableArrayList<Currency>()

    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val root = BorderPane()

    private val table = TableView<Currency>()

    private val currencyListener = MapChangeListener<Int, Currency> { this.onCurrencyUpdate(it) }

    init {
        val addHandler = EventHandler<ActionEvent> { openCurrencyDialog(null) }
        val editHandler = EventHandler<ActionEvent> {
            val currency = table.selectionModel.selectedItem
            if (currency != null) {
                openCurrencyDialog(currency)
            }
        }

        // Menu Bar
        val closeMenuItem = MenuItem(rb.getString("menu.File.Close")).apply {
            setOnAction { onClose() }
        }
        val fileMenu = Menu(rb.getString("menu.File"), null, closeMenuItem)

        val addMenuItem = MenuItem(rb.getString("menu.Edit.Add")).apply {
            onAction = addHandler
        }
        val editMenuItem = MenuItem(rb.getString("menu.Edit.Edit"))
        editMenuItem.onAction = editHandler
        val editMenu = Menu(rb.getString("menu.Edit"), null, addMenuItem, editMenuItem)

        val menuBar = MenuBar(fileMenu, editMenu, createHelpMenu(rb))
        menuBar.isUseSystemMenuBar = true

        // Context Menu
        val ctxAddMenuItem = MenuItem(rb.getString("menu.Edit.Add")).apply {
            onAction = addHandler
        }
        val ctxEditMenuItem = MenuItem(rb.getString("menu.Edit.Edit")).apply {
            onAction = editHandler
        }
        table.contextMenu = ContextMenu(ctxAddMenuItem, ctxEditMenuItem)

        // Table
        val colName = TableColumn<Currency, String>(rb.getString("column.Name"))
        val colDescription = TableColumn<Currency, String>(rb.getString("column.Description"))

        table.columns.setAll(colName, colDescription)

        root.setPrefSize(600.0, 400.0)
        root.top = menuBar
        root.center = table

        currencyList.addAll(MoneyDAO.currencies().values)
        table.items = currencyList
        colName.setCellValueFactory { p: TableColumn.CellDataFeatures<Currency, String> -> ReadOnlyObjectWrapper(p.value.symbol) }
        colDescription.setCellValueFactory { p: TableColumn.CellDataFeatures<Currency, String> -> ReadOnlyObjectWrapper(p.value.description) }

        editMenuItem.disableProperty()
                .bind(table.selectionModel.selectedItemProperty().isNull)
        ctxEditMenuItem.disableProperty()
                .bind(table.selectionModel.selectedItemProperty().isNull)

        MoneyDAO.currencies().addListener(WeakMapChangeListener(currencyListener))

        setupWindow(root)
    }

    override fun getTitle(): String = rb.getString("currency.Window.Title")

    private fun openCurrencyDialog(currency: Currency?) {
        CurrencyDialog(currency).showAndWait().ifPresent { c ->
            if (c.id != 0) {
                MoneyDAO.updateCurrency(c)
            } else {
                MoneyDAO.insertCurrency(c.copy(_id = MoneyDAO.generatePrimaryKey(Currency::class.java)))
            }
        }
    }

    private fun onCurrencyUpdate(change: MapChangeListener.Change<out Int, out Currency>) {
        if (change.wasAdded()) {
            val currency = change.valueAdded

            // find if we have item with this id
            val index = currencyList
                    .find { it.id == currency.id }
                    ?.let { currencyList.indexOf(it) }
                    ?:-1

            if (index != -1) {
                currencyList.removeAt(index)
                currencyList.add(index, currency)
            } else {
                // simply add
                currencyList.add(currency)
            }

            table.selectionModel.select(currency)
        }
    }

}