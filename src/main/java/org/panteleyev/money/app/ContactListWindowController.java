/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
import static org.panteleyev.fx.FxUtils.ELLIPSIS;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Bundles.translate;
import static org.panteleyev.money.app.Constants.ALL_TYPES_STRING;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_E;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_F;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_FILE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_ADD;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_EDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_ITEM_SEARCH;
import static org.panteleyev.money.bundles.Internationalization.I18N_MENU_VIEW;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_RESET_FILTER;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CLOSE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CONTACTS;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_PHONE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

class ContactListWindowController extends BaseController {
    private final ComboBox<ContactType> typeBox = comboBox(ContactType.values(),
        b -> b.withDefaultString(ALL_TYPES_STRING)
            .withStringConverter(Bundles::translate)
    );
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
            newMenu(fxString(UI, I18N_MENU_FILE),
                menuItem(fxString(UI, I18N_WORD_CLOSE), event -> onClose())),
            newMenu(fxString(UI, I18N_MENU_EDIT),
                menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), SHORTCUT_N, addHandler),
                menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), SHORTCUT_E, editHandler, disableBinding),
                new SeparatorMenuItem(),
                menuItem(fxString(UI, I18N_MENU_ITEM_SEARCH), SHORTCUT_F,
                    event -> searchField.requestFocus())),
            newMenu(fxString(UI, I18N_MENU_VIEW),
                menuItem(fxString(UI, I18N_MISC_RESET_FILTER), SHORTCUT_ALT_C, event -> resetFilter())),
            createWindowMenu(),
            createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context menu
        contactTable.setContextMenu(new ContextMenu(
            menuItem(fxString(UI, I18N_MENU_ITEM_ADD, ELLIPSIS), addHandler),
            menuItem(fxString(UI, I18N_MENU_ITEM_EDIT, ELLIPSIS), editHandler, disableBinding))
        );

        var w = contactTable.widthProperty().subtract(20);
        contactTable.getColumns().setAll(List.of(
            tableObjectColumn(fxString(UI, I18N_WORD_NAME), b ->
                b.withCellFactory(x -> new ContactNameCell()).withWidthBinding(w.multiply(0.4))),
            tableColumn(fxString(UI, I18N_WORD_TYPE), b ->
                b.withPropertyCallback((Contact p) -> translate(p.type())).withWidthBinding(w.multiply(0.2))),
            tableColumn(fxString(UI, I18N_WORD_PHONE), b ->
                b.withPropertyCallback(Contact::phone).withWidthBinding(w.multiply(0.2))),
            tableColumn("E-Mail", b ->
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
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Contact> getSelectedContact() {
        return Optional.ofNullable(contactTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return UI.getString(I18N_WORD_CONTACTS);
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
        new ContactDialog(this, settings().getDialogCssFileUrl(), null).showAndWait()
            .ifPresent(c -> dao().insertContact(c));
    }

    private void onEditContact() {
        getSelectedContact()
            .flatMap(selected ->
                new ContactDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
            .ifPresent(c -> dao().updateContact(c));
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
