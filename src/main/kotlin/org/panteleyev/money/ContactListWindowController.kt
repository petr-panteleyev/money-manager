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
import javafx.collections.transformation.FilteredList
import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ContextMenu
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.Separator
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import org.panteleyev.money.persistence.Contact
import org.panteleyev.money.persistence.ContactType
import org.panteleyev.money.persistence.MoneyDAO
import org.panteleyev.money.persistence.ReadOnlyStringConverter
import java.util.Arrays
import java.util.ResourceBundle

class ContactListWindowController : BaseController() {
    private val rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH)

    private val self = BorderPane()

    private val typeChoiceBox = ChoiceBox<Any>()
    private val contactTable = TableView<Contact>()

    private val contactsListener = MapChangeListener<Int, Contact> {
        Platform.runLater { this.reloadContacts() }
    }

    init {
        val addHandler = EventHandler<ActionEvent> { openContactDialog(null) }
        val editHandler = EventHandler<ActionEvent> { openContactDialog(contactTable.selectionModel.selectedItem) }

        // Menu bar
        val closeMenuItem = MenuItem(rb.getString("menu.File.Close")).apply {
            setOnAction { onClose() }
        }
        val fileMenu = Menu(rb.getString("menu.File"), null, closeMenuItem)

        val addMenuItem = MenuItem(rb.getString("menu.Edit.Add")).apply {
            onAction = addHandler
        }
        val editMenuItem = MenuItem(rb.getString("menu.Edit.Edit")).apply {
            onAction = editHandler
        }
        val editMenu = Menu(rb.getString("menu.Edit"), null, addMenuItem, editMenuItem)

        val menuBar = MenuBar(fileMenu, editMenu, createHelpMenu(rb)).apply {
            isUseSystemMenuBar = true
        }

        // Context menu
        val ctxAddMenuItem = MenuItem(rb.getString("menu.Edit.Add")).apply {
            onAction = addHandler
        }
        val ctxEditMenuItem = MenuItem(rb.getString("menu.Edit.Edit")).apply {
            onAction = editHandler
        }
        contactTable.contextMenu = ContextMenu(ctxAddMenuItem, ctxEditMenuItem)

        // Table
        val pane = BorderPane()

        val nameColumn = TableColumn<Contact, String>(rb.getString("column.Name"))
        val typeColumn = TableColumn<Contact, String>(rb.getString("column.Type"))
        val phoneColumn = TableColumn<Contact, String>(rb.getString("column.Phone"))
        val emailColumn = TableColumn<Contact, String>(rb.getString("column.Email"))

        contactTable.columns.setAll(Arrays.asList(
                nameColumn, typeColumn, phoneColumn, emailColumn))
        contactTable.onMouseClicked = EventHandler<MouseEvent> { this.onTableMouseClick(it) }

        // Toolbox
        val hBox = HBox(typeChoiceBox)

        pane.top = hBox
        pane.center = contactTable

        BorderPane.setMargin(hBox, Insets(5.0, 5.0, 5.0, 5.0))

        with (self) {
            setPrefSize(600.0, 400.0)
            top = menuBar
            center = pane
        }

        with (typeChoiceBox) {
            items.add(rb.getString("contact.Window.AllTypes"))
            items.add(Separator())
            items.addAll(*ContactType.values())
            selectionModel.clearAndSelect(0)
            converter = object : ReadOnlyStringConverter<Any>() {
                override fun toString(obj: Any): String {
                    if (obj is ContactType) {
                        return obj.typeName
                    } else {
                        return obj.toString()
                    }
                }
            }

            valueProperty().addListener { _, _, newValue -> onTypeChanged(newValue) }
        }

        nameColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Contact, String> -> ReadOnlyObjectWrapper(p.value.name) }
        typeColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Contact, String> -> ReadOnlyObjectWrapper(p.value.type.typeName) }
        phoneColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Contact, String> -> ReadOnlyObjectWrapper<String>(p.value.phone) }
        emailColumn.setCellValueFactory {
            p: TableColumn.CellDataFeatures<Contact, String> -> ReadOnlyObjectWrapper<String>(p.value.email) }

        editMenuItem.disableProperty()
                .bind(contactTable.selectionModel.selectedItemProperty().isNull)
        ctxEditMenuItem.disableProperty()
                .bind(contactTable.selectionModel.selectedItemProperty().isNull)

        reloadContacts()

        MoneyDAO.contacts().addListener(WeakMapChangeListener(contactsListener))

        setupWindow(self)
    }

    override fun getTitle(): String = rb.getString("contact.Window.Title")

    private fun reloadContacts() {
        val list = FXCollections.observableArrayList(MoneyDAO.getContacts())
        val filtered = list.filtered { true }
        contactTable.items = filtered
        onTypeChanged(typeChoiceBox.selectionModel.selectedItem)
    }

    private fun onTypeChanged(newValue: Any) {
        if (newValue is String) {
            (contactTable.items as FilteredList<Contact>).setPredicate { true }
        } else {
            val type = newValue as ContactType
            (contactTable.items as FilteredList<Contact>)
                    .setPredicate { it.type == type }
        }
    }

    private fun openContactDialog(contact: Contact?) {
        ContactDialog(contact)
                .showAndWait()
                .ifPresent { c ->
                    if (c.id != 0) {
                        MoneyDAO.updateContact(c)
                    } else {
                        MoneyDAO.insertContact(c.copy(id = MoneyDAO.generatePrimaryKey(Contact::class)))
                    }
                }
    }

    private fun onTableMouseClick(event: Event) {
        val me = event as MouseEvent
        if (me.clickCount == 2) {
            val c = contactTable.selectionModel.selectedItem
            if (c != null) {
                openContactDialog(c)
            }
        }
    }
}