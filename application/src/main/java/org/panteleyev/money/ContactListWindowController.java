/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.money.FXFactory.newMenuBar;
import static org.panteleyev.money.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class ContactListWindowController extends BaseController {
    private final ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private final TextField searchField = FXFactory.newSearchField(s -> updatePredicate());

    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();
    private final FilteredList<Contact> filteredList = new FilteredList<>(contacts);
    private final TableView<Contact> contactTable = new TableView<>(filteredList);

    @SuppressWarnings("FieldCanBeLocal")
    private MapChangeListener<UUID, Contact> contactsListener = change ->
        Platform.runLater(this::reloadContacts);

    ContactListWindowController() {
        EventHandler<ActionEvent> addHandler = event -> onAddContact();
        EventHandler<ActionEvent> editHandler = event -> onEditContact();

        var disableBinding = contactTable.getSelectionModel().selectedItemProperty().isNull();

        // Menu bar
        var menuBar = newMenuBar(
            new Menu(RB.getString("menu.File"), null,
                newMenuItem(RB, "menu.File.Close", event -> onClose())),
            new Menu(RB.getString("menu.Edit"), null,
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.Edit.Search",
                    new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                    actionEvent -> searchField.requestFocus())),
            createHelpMenu(RB));

        // Context menu
        contactTable.setContextMenu(new ContextMenu(
            newMenuItem(RB, "menu.Edit.Add", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var pane = new BorderPane();

        var nameColumn = new TableColumn<Contact, Contact>(RB.getString("column.Name"));
        var typeColumn = new TableColumn<Contact, String>(RB.getString("column.Type"));
        var phoneColumn = new TableColumn<Contact, String>(RB.getString("column.Phone"));
        var emailColumn = new TableColumn<Contact, String>(RB.getString("column.Email"));

        contactTable.getColumns().setAll(List.of(nameColumn, typeColumn, phoneColumn, emailColumn));
        contactTable.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        var hBox = new HBox(5, searchField, typeChoiceBox);
        pane.setTop(hBox);
        pane.setCenter(contactTable);

        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var self = new BorderPane();
        self.setPrefSize(600.0, 400.0);
        self.setTop(menuBar);
        self.setCenter(pane);

        typeChoiceBox.getItems().add(RB.getString("contact.Window.AllTypes"));
        typeChoiceBox.getItems().add(new Separator());
        typeChoiceBox.getItems().addAll(ContactType.values());
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                return obj instanceof ContactType ? ((ContactType) obj).getTypeName() : obj.toString();
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, Contact> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        nameColumn.setCellFactory(x -> new ContactNameCell());
        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getType().getTypeName()));
        phoneColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getPhone()));
        emailColumn.setCellValueFactory((TableColumn.CellDataFeatures<Contact, String> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getEmail()));

        reloadContacts();
        cache().contacts().addListener(new WeakMapChangeListener<>(contactsListener));
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
        contacts.setAll(cache().getContacts().stream()
            .sorted(Comparator.comparing(Contact::getName)).collect(Collectors.toList()));
        updatePredicate();
    }

    private Predicate<Contact> getPredicate() {
        Predicate<Contact> filter;

        // Type
        var type = typeChoiceBox.getSelectionModel().getSelectedItem();
        if (type instanceof String) {
            filter = x -> true;
        } else {
            filter = x -> x.getType() == type;
        }

        // Name
        var search = searchField.getText().toLowerCase();
        if (!search.isEmpty()) {
            filter = filter.and(x -> x.getName().toLowerCase().contains(search));
        }

        return filter;
    }

    private void onAddContact() {
        new ContactDialog(null).showAndWait()
            .ifPresent(c -> getDao().insertContact(c));
    }

    private void onEditContact() {
        getSelectedContact().ifPresent(selected ->
            new ContactDialog(selected).showAndWait().ifPresent(c -> getDao().updateContact(c)));
    }

    private void onTableMouseClick(Event event) {
        if (((MouseEvent) event).getClickCount() == 2) {
            onEditContact();
        }
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }
}
