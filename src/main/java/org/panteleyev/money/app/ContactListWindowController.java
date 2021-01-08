/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Constants.ALL_TYPES_STRING;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.Constants.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_F;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.options.Options.options;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class ContactListWindowController extends BaseController {
    private final ComboBox<ContactType> typeBox = comboBox(ContactType.values(),
        b -> b.withDefaultString(ALL_TYPES_STRING));
    private final TextField searchField = newSearchField(SEARCH_FIELD_FACTORY, s -> updatePredicate());

    private final FilteredList<Contact> filteredList = cache().getContacts().filtered(x -> true);
    private final SortedList<Contact> sortedList = filteredList.sorted(Comparator.comparing(Contact::name));
    private final TableView<Contact> contactTable = new TableView<>(sortedList);

    ContactListWindowController() {
        EventHandler<ActionEvent> addHandler = event -> onAddContact();
        EventHandler<ActionEvent> editHandler = event -> onEditContact();

        var disableBinding = contactTable.getSelectionModel().selectedItemProperty().isNull();

        // Menu bar
        var menuBar = menuBar(
            newMenu(fxString(RB, "File"),
                menuItem(fxString(RB, "Close"), event -> onClose())),
            newMenu(fxString(RB, "menu.Edit"),
                menuItem(fxString(RB, "Create"), SHORTCUT_N, addHandler),
                menuItem(fxString(RB, "menu.Edit.Edit"), SHORTCUT_E, editHandler, disableBinding),
                new SeparatorMenuItem(),
                menuItem(fxString(RB, "menu.Edit.Search"), SHORTCUT_F,
                    event -> searchField.requestFocus())),
            newMenu(fxString(RB, "View"),
                menuItem(fxString(RB, "Reset_Filter"), SHORTCUT_ALT_C, event -> resetFilter())),
            createWindowMenu(),
            createHelpMenu());

        // Context menu
        contactTable.setContextMenu(new ContextMenu(
            menuItem(fxString(RB, "Create"), addHandler),
            menuItem(fxString(RB, "menu.Edit.Edit"), editHandler, disableBinding))
        );

        var w = contactTable.widthProperty().subtract(20);
        contactTable.getColumns().setAll(List.of(
            tableObjectColumn(fxString(RB, "Name"), b ->
                b.withCellFactory(x -> new ContactNameCell()).withWidthBinding(w.multiply(0.4))),
            tableColumn(fxString(RB, "Type"), b ->
                b.withPropertyCallback((Contact p) -> p.type().toString()).withWidthBinding(w.multiply(0.2))),
            tableColumn(fxString(RB, "Phone"), b ->
                b.withPropertyCallback(Contact::phone).withWidthBinding(w.multiply(0.2))),
            tableColumn(fxString(RB, "Email"), b ->
                b.withPropertyCallback(Contact::email).withWidthBinding(w.multiply(0.2)))
        ));
        contactTable.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        var hBox = hBox(5, searchField, typeBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var self = new BorderPane(
            new BorderPane(contactTable, hBox, null, null, null),
            menuBar, null, null, null
        );
        self.setPrefSize(600.0, 400.0);

        typeBox.getSelectionModel().select(0);

        typeBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        reloadContacts();
        setupWindow(self);

        Platform.runLater(this::resetFilter);
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
        // Type
        var type = typeBox.getSelectionModel().getSelectedItem();
        Predicate<Contact> filter = type == null ? x -> true : x -> x.type() == type;

        // Name
        var search = searchField.getText().toLowerCase();
        if (!search.isEmpty()) {
            filter = filter.and(x -> x.name().toLowerCase().contains(search));
        }

        return filter;
    }

    private void onAddContact() {
        new ContactDialog(this, options().getDialogCssFileUrl(), null).showAndWait()
            .ifPresent(c -> getDao().insertContact(c));
    }

    private void onEditContact() {
        getSelectedContact()
            .flatMap(selected ->
                new ContactDialog(this, options().getDialogCssFileUrl(), selected).showAndWait())
            .ifPresent(c -> getDao().updateContact(c));
    }

    private void onTableMouseClick(Event event) {
        if (((MouseEvent) event).getClickCount() == 2) {
            onEditContact();
        }
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }

    private void resetFilter() {
        clearValueAndSelection(typeBox);
        searchField.setText("");
    }
}
