package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
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
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
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
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class ContactListWindowController extends BaseController {
    private final ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private final TextField searchField = newSearchField(Images.SEARCH, s -> updatePredicate());

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
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.Edit.Search",
                    new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                    actionEvent -> searchField.requestFocus())),
            createWindowMenu(RB),
            createHelpMenu(RB));

        // Context menu
        contactTable.setContextMenu(new ContextMenu(
            newMenuItem(RB, "menu.Edit.Add", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

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
        typeChoiceBox.getItems().addAll(ContactType.values());
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                return obj instanceof ContactType type ? type.getTypeName() : obj.toString();
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        reloadContacts();
        cache().contacts().addListener(new WeakMapChangeListener<>(contactsListener));
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
        contacts.setAll(cache().getContacts().stream()
            .sorted(Comparator.comparing(Contact::name)).collect(Collectors.toList()));
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
