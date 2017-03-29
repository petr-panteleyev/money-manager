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

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import org.panteleyev.money.persistence.*;

public class ContactListWindowController extends BaseController implements Initializable {
    @FXML private Parent        self;

    @FXML private TableColumn<Contact, String>  nameColumn;
    @FXML private TableColumn<Contact, String>  typeColumn;
    @FXML private TableColumn<Contact, String>  phoneColumn;
    @FXML private TableColumn<Contact, String>  emailColumn;

    @FXML private ChoiceBox                     typeChoiceBox;
    @FXML private TableView<Contact>            contactTable;

    @FXML private MenuBar                       menuBar;
    @FXML private MenuItem                      editMenuItem;
    @FXML private MenuItem                      ctxEditMenuItem;

    private ResourceBundle bundle;

    private final SimpleMapProperty<Integer, Contact> contactsProperty = new SimpleMapProperty<>();

    public ContactListWindowController() {
        super("/org/panteleyev/money/ContactListWindow.fxml", MainWindowController.UI_BUNDLE_PATH, true);

        contactsProperty.bind(MoneyDAO.getInstance().contactsProperty());
        contactsProperty.addListener((x,y,z) -> Platform.runLater(this::reloadContacts));
    }

    @Override
    protected Parent getSelf() {
        return self;
    }

    @Override
    public String getTitle() {
        return bundle == null? "Contacts" : bundle.getString("contact.Window.Title");
    }

    @Override
    public void initialize(URL location, ResourceBundle rb) {
        this.bundle = rb;

        menuBar.setUseSystemMenuBar(true);

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
                .load()
                .showAndWait()
                .ifPresent(builder -> {
                    if (builder.id().isPresent()) {
                        dao.updateContact(builder.build());
                    } else {
                        builder = builder.id(dao.generatePrimaryKey(Contact.class));
                        dao.insertContact(builder.build());
                    }
                });
    }

    public void onAddContact() {
        openContactDialog(null);
    }

    public void onEditContact() {
        openContactDialog(contactTable.getSelectionModel().getSelectedItem());
    }

    public void onTableMouseClick(Event event) {
        MouseEvent me = (MouseEvent)event;
        if (me.getClickCount() == 2) {
            Contact c = contactTable.getSelectionModel().getSelectedItem();
            if (c != null) {
                openContactDialog(c);
            }
        }
    }

    @Override
    public void onClose() {
        contactsProperty.unbind();
        super.onClose();
    }
}
