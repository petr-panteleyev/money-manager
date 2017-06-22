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
import javafx.collections.WeakMapChangeListener
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import org.panteleyev.money.persistence.Category
import org.panteleyev.money.persistence.MoneyDAO
import java.util.ResourceBundle

class CategoryWindowController : BaseController() {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val categoryList = FXCollections.observableArrayList<Category>()

    private val categoryTable = TableView<Category>()

    private val self = BorderPane()

    private val categoriesListener = MapChangeListener<Int, Category> {
        Platform.runLater { this.updateWindow() }
    }

    init {
        // Event handlers
        val addHandler = EventHandler<ActionEvent> { onMenuAdd() }
        val editHandler = EventHandler<ActionEvent> { onMenuEdit() }

        // Main Menu
        val closeMenuItem = MenuItem(rb.getString("menu.File.Close"))
        closeMenuItem.setOnAction { onClose() }
        val fileMenu = Menu(rb.getString("menu.File"), null, closeMenuItem)

        val addMenuItem = MenuItem(rb.getString("menu.Edit.Add"))
        addMenuItem.onAction = addHandler
        val editMenuItem = MenuItem(rb.getString("menu.Edit.Edit"))
        editMenuItem.onAction = editHandler
        val editMenu = Menu(rb.getString("menu.Edit"), null, addMenuItem, editMenuItem)

        val menuBar = MenuBar(fileMenu, editMenu, createHelpMenu(rb))
        menuBar.isUseSystemMenuBar = true

        // Context Menu
        val ctxAddMenuItem = MenuItem(rb.getString("menu.Edit.Add"))
        ctxAddMenuItem.onAction = addHandler
        val ctxEditMenuItem = MenuItem(rb.getString("menu.Edit.Edit"))
        ctxEditMenuItem.onAction = editHandler
        categoryTable.contextMenu = ContextMenu(ctxAddMenuItem, ctxEditMenuItem)

        // Table
        val colType = TableColumn<Category, String>(rb.getString("column.Type"))
        val colName = TableColumn<Category, String>(rb.getString("column.Name"))
        val colDescription = TableColumn<Category, String>(rb.getString("column.Description"))

        categoryTable.columns.setAll(colType, colName, colDescription)

        categoryTable.onMouseClicked = EventHandler<MouseEvent> { this.onTableMouseClick(it) }

        self.setPrefSize(600.0, 400.0)
        self.top = menuBar
        self.center = categoryTable

        categoryTable.items = categoryList
        updateList()

        colType.setCellValueFactory { p: TableColumn.CellDataFeatures<Category, String> -> ReadOnlyObjectWrapper(p.value.type.typeName) }
        colName.setCellValueFactory { p: TableColumn.CellDataFeatures<Category, String> -> ReadOnlyObjectWrapper(p.value.name) }
        colDescription.setCellValueFactory { p: TableColumn.CellDataFeatures<Category, String> -> ReadOnlyObjectWrapper(p.value.comment) }

        colType.isSortable = true
        categoryTable.sortOrder.add(colType)
        colType.sortType = TableColumn.SortType.ASCENDING

        editMenuItem.disableProperty().bind(categoryTable.selectionModel.selectedItemProperty().isNull)
        ctxEditMenuItem.disableProperty().bind(categoryTable.selectionModel.selectedItemProperty().isNull)

        colType.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2))
        colName.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2))
        colDescription.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.6))

        MoneyDAO.categories().addListener(WeakMapChangeListener(categoriesListener))
        setupWindow(self)
    }

    override fun getTitle(): String = rb.getString("category.Window.Title")

    private fun updateList() {
        categoryList.setAll(MoneyDAO.getCategories())
    }

    private fun onTableMouseClick(event: Event) {
        val me = event as MouseEvent
        if (me.clickCount == 2) {
            val category = categoryTable.selectionModel.selectedItem
            if (category != null) {
                openCategoryDialog(category)
            }
        }
    }

    private fun onMenuEdit() {
        val category = categoryTable.selectionModel.selectedItem
        if (category != null) {
            openCategoryDialog(category)
        }
    }

    private fun onMenuAdd() {
        openCategoryDialog(null)
    }

    private fun openCategoryDialog(category: Category?) {
        CategoryDialog(category).showAndWait().ifPresent { c ->
            if (c.id != 0) {
                MoneyDAO.updateCategory(c)
            } else {
                MoneyDAO.insertCategory(c.copy(_id = MoneyDAO.generatePrimaryKey(Category::class.java)))
            }
        }
    }

    private fun updateWindow() {
        val selIndex = categoryTable.selectionModel.selectedIndex
        categoryList.setAll(MoneyDAO.categories().values)
        categoryTable.selectionModel.select(selIndex)
    }

}