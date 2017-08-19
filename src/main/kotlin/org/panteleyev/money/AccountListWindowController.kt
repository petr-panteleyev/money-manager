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

import javafx.application.Platform
import javafx.beans.property.ReadOnlyObjectWrapper
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableList
import javafx.collections.WeakMapChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.CheckBox
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.panteleyev.money.persistence.Account
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.CategoryType
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import java.math.BigDecimal
import java.util.Arrays
import java.util.Optional
import java.util.ResourceBundle

class AccountListWindowController : BaseController() {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val self = BorderPane()

    private val typeChoiceBox = ChoiceBox<Any>()
    private val categoryChoiceBox = ChoiceBox<Any>()
    private val showActiveCheckBox = CheckBox(rb.getString("account.Window.ShowOnlyActive"))
    private val accountListTable = TableView<Account>()

    private val accountsListener = MapChangeListener<Int, Account> {
        Platform.runLater { this.reloadAccounts() } }

    init {
        // Event handlers
        val addHandler = EventHandler<ActionEvent> { onAddAccount() }
        val editHandler = EventHandler<ActionEvent> { onEditAccount() }
        val deleteHandler = EventHandler<ActionEvent> { onDeleteAccount() }

        // Main menu
        val closeMenuItem = MenuItem(rb.getString("menu.File.Close"))
        closeMenuItem.setOnAction { onClose() }
        val fileMenu = Menu(rb.getString("menu.File"), null, closeMenuItem)

        val addMenuItem = MenuItem(rb.getString("menu.Edit.Add"))
        addMenuItem.onAction = addHandler
        val editMenuItem = MenuItem(rb.getString("menu.Edit.Edit"))
        editMenuItem.onAction = editHandler
        val deleteMenuItem = MenuItem(rb.getString("menu.Edit.Delete"))
        deleteMenuItem.onAction = deleteHandler
        val editMenu = Menu(rb.getString("menu.Edit"), null,
                addMenuItem, editMenuItem, SeparatorMenuItem(), deleteMenuItem)

        val menuBar = MenuBar(fileMenu, editMenu, createHelpMenu(rb))
        menuBar.isUseSystemMenuBar = true

        // Context menu
        val ctxAddMenuItem = MenuItem(rb.getString("menu.Edit.Add"))
        ctxAddMenuItem.onAction = addHandler
        val ctxEditMenuItem = MenuItem(rb.getString("menu.Edit.Edit"))
        ctxEditMenuItem.onAction = editHandler
        val ctxDeleteMenuItem = MenuItem(rb.getString("menu.Edit.Delete"))
        ctxDeleteMenuItem.onAction = deleteHandler
        accountListTable.contextMenu = ContextMenu(ctxAddMenuItem, ctxEditMenuItem, SeparatorMenuItem(), ctxDeleteMenuItem)

        // Table
        val idColumn = TableColumn<Account, Int>("ID")
        val nameColumn = TableColumn<Account, String>(rb.getString("column.Name"))
        val typeColumn = TableColumn<Account, String>(rb.getString("column.Type"))
        val categoryColumn = TableColumn<Account, String>(rb.getString("column.Category"))
        val currencyColumn = TableColumn<Account, String>(rb.getString("column.Currency"))
        val balanceColumn = TableColumn<Account, BigDecimal>(rb.getString("column.InitialBalance"))
        val activeColumn = TableColumn<Account, CheckBox>("A")

        accountListTable.columns.setAll(Arrays.asList(
                idColumn, nameColumn, typeColumn, categoryColumn,
                currencyColumn, balanceColumn, activeColumn))

        // Content
        val pane = BorderPane()

        // Toolbox
        val hBox = HBox(typeChoiceBox, categoryChoiceBox, showActiveCheckBox)
        hBox.alignment = Pos.CENTER_LEFT
        HBox.setMargin(categoryChoiceBox, Insets(0.0, 0.0, 0.0, 5.0))
        HBox.setMargin(showActiveCheckBox, Insets(0.0, 0.0, 0.0, 10.0))

        pane.top = hBox
        pane.center = accountListTable
        BorderPane.setMargin(hBox, Insets(5.0, 5.0, 5.0, 5.0))

        self.setPrefSize(800.0, 400.0)
        self.top = menuBar
        self.center = pane

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        val types = FXCollections.observableArrayList<Any>(*CategoryType.values())
        if (!types.isEmpty()) {
            types.add(0, Separator())
        }
        types.add(0, rb.getString("account.Window.AllTypes"))

        typeChoiceBox.items = types
        typeChoiceBox.selectionModel.clearAndSelect(0)

        typeChoiceBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj: Any?): String {
                if (obj is CategoryType) {
                    return obj.typeName
                } else {
                    return obj?.toString() ?: "-"
                }
            }
        }

        typeChoiceBox.valueProperty().addListener { _, _, newValue -> onTypeChanged(newValue) }

        categoryChoiceBox.converter = object : ReadOnlyStringConverter<Any>() {
            override fun toString(obj : Any?): String {
                if (obj is Category) {
                    return obj.name
                } else {
                    return obj?.toString() ?: "-"
                }
            }
        }

        categoryChoiceBox.items = FXCollections.observableArrayList<Any>(rb.getString("account.Window.AllCategories"))
        categoryChoiceBox.selectionModel.clearAndSelect(0)
        categoryChoiceBox.valueProperty().addListener { _, _, _ -> reloadAccounts() }

        showActiveCheckBox.setOnAction { reloadAccounts() }

        idColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, Int> -> ReadOnlyObjectWrapper(p.value.id) }
        nameColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, String> -> ReadOnlyObjectWrapper(p.value.name) }

        typeColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, String> -> ReadOnlyObjectWrapper(p.value.type.typeName) }
        categoryColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, String> ->
            ReadOnlyObjectWrapper<String>(MoneyDAO.getCategory(p.value.categoryId)?.name?:"")
        }
        currencyColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, String> ->
            ReadOnlyObjectWrapper(MoneyDAO.getCurrency(p.value.currencyId)?.symbol?:"")
        }
        balanceColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, BigDecimal> -> ReadOnlyObjectWrapper(p.value.openingBalance.setScale(2, BigDecimal.ROUND_HALF_UP)) }
        activeColumn.setCellValueFactory { p: TableColumn.CellDataFeatures<Account, CheckBox> ->
            val account = p.value

            val cb = CheckBox()
            cb.isSelected = p.value.enabled

            cb.setOnAction {
                val enabled = account.enabled
                MoneyDAO.updateAccount(account.enable(!enabled))
            }

            ReadOnlyObjectWrapper(cb)
        }

        editMenuItem.disableProperty()
                .bind(accountListTable.selectionModel.selectedItemProperty().isNull)
        deleteMenuItem.disableProperty()
                .bind(accountListTable.selectionModel.selectedItemProperty().isNull)
        ctxEditMenuItem.disableProperty()
                .bind(accountListTable.selectionModel.selectedItemProperty().isNull)
        ctxDeleteMenuItem.disableProperty()
                .bind(accountListTable.selectionModel.selectedItemProperty().isNull)

        reloadAccounts()

        MoneyDAO.accounts().addListener(WeakMapChangeListener(accountsListener))

        setupWindow(self)
    }

    override fun getTitle(): String = rb.getString("account.Window.Title")

    private fun reloadAccounts() {
        var accounts: Collection<Account>

        val catObject = categoryChoiceBox.selectionModel.selectedItem
        if (catObject is Category) {
            accounts = MoneyDAO.getAccountsByCategory(catObject.id)
        } else {
            val typeObject = typeChoiceBox.selectionModel.selectedItem
            if (typeObject is CategoryType) {
                accounts = MoneyDAO.getAccountsByType(typeObject)
            } else {
                accounts = MoneyDAO.getAccounts()
            }
        }

        if (showActiveCheckBox.isSelected) {
            accounts = accounts.filter { it.enabled }
        }

        accountListTable.items = FXCollections.observableArrayList(accounts)
    }

    private fun onTypeChanged(newValue: Any) {
        val items: ObservableList<Any>

        if (newValue is String) {
            items = FXCollections.observableArrayList<Any>(rb.getString("account.Window.AllCategories"))
        } else {
            items = FXCollections.observableArrayList<Any>(
                    MoneyDAO.getCategoriesByType(newValue as CategoryType)
            )

            if (!items.isEmpty()) {
                items.add(0, Separator())
            }
            items.add(0, rb.getString("account.Window.AllCategories"))
        }

        categoryChoiceBox.items = items
        categoryChoiceBox.selectionModel.clearAndSelect(0)
    }

    private fun onAddAccount() {
        AccountDialog(null, null).showAndWait()
                .ifPresent { a ->
                    MoneyDAO.insertAccount(a.copy(id = MoneyDAO.generatePrimaryKey(Account::class)))
                }
    }

    private fun getSelectedAccount(): Optional<Account> {
        return Optional.of(accountListTable.selectionModel.selectedItem)
    }

    private fun onEditAccount() {
        getSelectedAccount().ifPresent { account ->
            AccountDialog(account, null).showAndWait()
                    .ifPresent { MoneyDAO.updateAccount(it) }
        }
    }

    private fun onDeleteAccount() {
        getSelectedAccount().ifPresent { account ->
            val count = MoneyDAO.getTransactionCount(account)
            if (count != 0) {
                Alert(Alert.AlertType.ERROR, "Unable to delete account\nwith $count associated transactions", ButtonType.CLOSE)
                        .showAndWait()
            } else {
                Alert(Alert.AlertType.CONFIRMATION, rb.getString("text.AreYouSure"), ButtonType.OK, ButtonType.CANCEL).showAndWait()
                        .filter { response -> response == ButtonType.OK }
                        .ifPresent { MoneyDAO.deleteAccount(account) }
            }
        }
    }

}