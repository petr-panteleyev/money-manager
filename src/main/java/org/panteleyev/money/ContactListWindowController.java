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
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.ContactType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.Arrays;
import java.util.Optional;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class ContactListWindowController extends BaseController {
    private BorderPane self = new BorderPane();

    private ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private TableView<Contact> contactTable = new TableView<>();

    private MapChangeListener<Integer, Contact> contactsListener = change ->
            Platform.runLater(this::reloadContacts);


    public ContactListWindowController() {
        EventHandler<ActionEvent> addHandler = event -> openContactDialog(null);
        EventHandler<ActionEvent> editHandler = event -> getSelectedContact().ifPresent(this::openContactDialog);

        // Menu bar
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

        // Context menu
        MenuItem ctxAddMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);

        MenuItem ctxEditMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);

        contactTable.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        BorderPane pane = new BorderPane();

        TableColumn<Contact, String> nameColumn = new TableColumn<>(RB.getString("column.Name"));
        TableColumn<Contact, String> typeColumn = new TableColumn<>(RB.getString("column.Type"));
        TableColumn<Contact, String> phoneColumn = new TableColumn<>(RB.getString("column.Phone"));
        TableColumn<Contact, String> emailColumn = new TableColumn<>(RB.getString("column.Email"));

        contactTable.getColumns().setAll(Arrays.asList(
                nameColumn, typeColumn, phoneColumn, emailColumn));
        contactTable.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        HBox hBox = new HBox(typeChoiceBox);

        pane.setTop(hBox);
        pane.setCenter(contactTable);

        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        self.setPrefSize(600.0, 400.0);
        self.setTop(menuBar);
        self.setCenter(pane);

        typeChoiceBox.getItems().add(RB.getString("contact.Window.AllTypes"));
        typeChoiceBox.getItems().add(new Separator());
        typeChoiceBox.getItems().addAll(ContactType.values());
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter() {
            public String toString(Object obj) {
                if (obj instanceof ContactType) {
                    return ((ContactType) obj).getTypeName();
                } else {
                    return obj.toString();
                }
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> onTypeChanged(newValue));

        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getName()));
        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getType().getTypeName()));
        phoneColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getPhone()));
        emailColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getEmail()));

        editMenuItem.disableProperty()
                .bind(contactTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
                .bind(contactTable.getSelectionModel().selectedItemProperty().isNull());

        reloadContacts();

        getDao().contacts().addListener(new WeakMapChangeListener<>(contactsListener));

        setupWindow(self);
    }

    private Optional<Contact> getSelectedContact() {
        return Optional.ofNullable(contactTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("contact.Window.Title");
    }

    private void reloadContacts() {
        ObservableList<Contact> list = FXCollections.observableArrayList(getDao().getContacts());
        contactTable.setItems(list.filtered(x -> true));
        onTypeChanged(typeChoiceBox.getSelectionModel().getSelectedItem());
    }

    private void onTypeChanged(Object newValue) {
        if (newValue instanceof String) {
            ((FilteredList<Contact>) contactTable.getItems()).setPredicate(x -> true);
        } else {
            ContactType type = (ContactType) newValue;
            ((FilteredList<Contact>) contactTable.getItems()).setPredicate(x -> x.getType().equals(type));
        }
    }

    private void openContactDialog(Contact contact) {
        new ContactDialog(contact).showAndWait()
                .ifPresent(c -> {
                    if (c.getId() != 0) {
                        getDao().updateContact(c);
                    } else {
                        getDao().insertContact(c.copy(getDao().generatePrimaryKey(Contact.class)));
                    }
                });
    }

    private void onTableMouseClick(Event event) {
        if (((MouseEvent) event).getClickCount() == 2) {
            getSelectedContact().ifPresent(this::openContactDialog);
        }
    }
}
