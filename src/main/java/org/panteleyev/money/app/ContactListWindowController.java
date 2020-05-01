package org.panteleyev.money.app;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_F;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class ContactListWindowController extends BaseController {
    private final ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private final TextField searchField = newSearchField(Images.SEARCH, s -> updatePredicate());

    private final FilteredList<Contact> filteredList = cache().getContacts().filtered(x -> true);
    private final SortedList<Contact> sortedList = filteredList.sorted(Comparator.comparing(Contact::name));
    private final TableView<Contact> contactTable = new TableView<>(sortedList);

    ContactListWindowController() {
        EventHandler<ActionEvent> addHandler = event -> onAddContact();
        EventHandler<ActionEvent> editHandler = event -> onEditContact();

        var disableBinding = contactTable.getSelectionModel().selectedItemProperty().isNull();

        // Menu bar
        var menuBar = newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "Create", SHORTCUT_N, addHandler),
                newMenuItem(RB, "menu.Edit.Edit", SHORTCUT_E, editHandler, disableBinding),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.Edit.Search", SHORTCUT_F,
                    event -> searchField.requestFocus())),
            createWindowMenu(),
            createHelpMenu());

        // Context menu
        contactTable.setContextMenu(new ContextMenu(
            newMenuItem(RB, "Create", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding))
        );

        // Table
        var pane = new BorderPane();

        var w = contactTable.widthProperty().subtract(20);
        contactTable.getColumns().setAll(List.of(
            newTableColumn(RB, "Name", x -> new ContactNameCell(), w.multiply(0.4)),
            newTableColumn(RB, "Type", null, (Contact p) -> p.type().getTypeName(), w.multiply(0.2)),
            newTableColumn(RB, "Phone", null, Contact::phone, w.multiply(0.2)),
            newTableColumn(RB, "Email", null, Contact::email, w.multiply(0.2))
        ));
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

        typeChoiceBox.getItems().add(RB.getString("All_Types"));
        typeChoiceBox.getItems().add(new Separator());
        typeChoiceBox.getItems().addAll(Arrays.asList(ContactType.values()));
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                return obj instanceof ContactType type ? type.getTypeName() : obj.toString();
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        reloadContacts();
        setupWindow(self);
    }

    private Optional<Contact> getSelectedContact() {
        return Optional.ofNullable(contactTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("Contacts");
    }

    private void reloadContacts() {
        updatePredicate();
    }

    private Predicate<Contact> getPredicate() {
        Predicate<Contact> filter;

        // Type
        var type = typeChoiceBox.getSelectionModel().getSelectedItem();
        if (type instanceof String) {
            filter = x -> true;
        } else {
            filter = x -> x.type() == type;
        }

        // Name
        var search = searchField.getText().toLowerCase();
        if (!search.isEmpty()) {
            filter = filter.and(x -> x.name().toLowerCase().contains(search));
        }

        return filter;
    }

    private void onAddContact() {
        new ContactDialog(this, null).showAndWait()
            .ifPresent(c -> getDao().insertContact(c));
    }

    private void onEditContact() {
        getSelectedContact().flatMap(selected ->
            new ContactDialog(this, selected).showAndWait()).ifPresent(c -> getDao().updateContact(c));
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
