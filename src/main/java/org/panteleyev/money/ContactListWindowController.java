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
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.ResourceBundle;

class ContactListWindowController extends BaseController {
    private final ResourceBundle     rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final BorderPane         self = new BorderPane();

    private final ChoiceBox          typeChoiceBox = new ChoiceBox();
    private final TableView<Contact> contactTable = new TableView<>();

    private final MapChangeListener<Integer,Contact> contactsListener =
            (MapChangeListener<Integer,Contact>)l -> Platform.runLater(this::reloadContacts);

    ContactListWindowController() {
        super(null);
        initialize();
        setupWindow(self);
    }

    @Override
    public String getTitle() {
        return rb.getString("contact.Window.Title");
    }

    private void initialize() {
        EventHandler<ActionEvent> addHandler = (evt) -> openContactDialog(null);
        EventHandler<ActionEvent> editHandler = (evt) ->
                openContactDialog(contactTable.getSelectionModel().getSelectedItem());

        // Menu bar
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

        // Context menu
        MenuItem ctxAddMenuItem = new MenuItem(rb.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);
        MenuItem ctxEditMenuItem = new MenuItem(rb.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);
        contactTable.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        BorderPane pane = new BorderPane();

        TableColumn<Contact, String>  nameColumn = new TableColumn<>(rb.getString("column.Name"));
        TableColumn<Contact, String>  typeColumn = new TableColumn<>(rb.getString("column.Type"));
        TableColumn<Contact, String>  phoneColumn = new TableColumn<>(rb.getString("column.Phone"));
        TableColumn<Contact, String>  emailColumn = new TableColumn<>(rb.getString("column.Email"));

        contactTable.getColumns().setAll(nameColumn, typeColumn, phoneColumn, emailColumn);
        contactTable.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        HBox hBox = new HBox(typeChoiceBox);

        pane.setTop(hBox);
        pane.setCenter(contactTable);

        BorderPane.setMargin(hBox, new Insets(5, 5, 5, 5));

        self.setPrefSize(600, 400);
        self.setTop(menuBar);
        self.setCenter(pane);

        typeChoiceBox.getItems().add(rb.getString("contact.Window.AllTypes"));
        typeChoiceBox.getItems().add(new Separator());
        typeChoiceBox.getItems().addAll(ContactType.values());

        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object object) {
                if (object instanceof ContactType) {
                    return ((ContactType)object).getName();
                } else {
                    return object.toString();
                }
            }
        });

        typeChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> onTypeChanged(newValue));

        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper(p.getValue().getName()));
        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getType().getName()));
        phoneColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper(p.getValue().getPhone()));
        emailColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper(p.getValue().getEmail()));

        editMenuItem.disableProperty()
                .bind(contactTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
                .bind(contactTable.getSelectionModel().selectedItemProperty().isNull());

        reloadContacts();

        MoneyDAO.getInstance().contacts()
                .addListener(new WeakMapChangeListener<>(contactsListener));
    }

    private void reloadContacts() {
        ObservableList<Contact> list = FXCollections.observableArrayList(MoneyDAO.getInstance().getContacts());
        FilteredList<Contact> filtered = list.filtered(x -> true);
        contactTable.setItems(filtered);
        onTypeChanged(typeChoiceBox.getSelectionModel().getSelectedItem());
    }

    private void onTypeChanged(Object newValue) {
        if (newValue instanceof String) {
            ((FilteredList)contactTable.getItems()).setPredicate(x -> true);
        } else {
            ContactType type = (ContactType)newValue;
            ((FilteredList<Contact>)contactTable.getItems())
                    .setPredicate(x -> x.getType().equals(type));
        }
    }

    private void openContactDialog(Contact contact) {
        MoneyDAO dao = MoneyDAO.getInstance();
        new ContactDialog(contact)
                .showAndWait()
                .ifPresent(builder -> {
                    if (builder.id() != 0) {
                        dao.updateContact(builder.build());
                    } else {
                        builder = builder.id(dao.generatePrimaryKey(Contact.class));
                        dao.insertContact(builder.build());
                    }
                });
    }

    private void onTableMouseClick(Event event) {
        MouseEvent me = (MouseEvent)event;
        if (me.getClickCount() == 2) {
            Contact c = contactTable.getSelectionModel().getSelectedItem();
            if (c != null) {
                openContactDialog(c);
            }
        }
    }
}
