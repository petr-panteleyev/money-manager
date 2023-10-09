/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.contact;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Bundles.translate;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class ContactListWindowController extends BaseController {
    private static final int NAME_COLUMN_INDEX = 0;

    private final ComboBox<ContactType> typeBox = comboBox(ContactType.values(),
            b -> b.withDefaultString("Все типы")
                    .withStringConverter(Bundles::translate)
    );
    private final TextField searchField = newSearchField(SEARCH_FIELD_FACTORY, s -> updatePredicate());

    private final FilteredList<Contact> filteredList = cache().getContacts().filtered(x -> true);
    private final SortedList<Contact> sortedList = filteredList.sorted();
    private final TableView<Contact> table = new TableView<>(sortedList);

    public ContactListWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateContact, this::onEditContact, event -> {},
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        // Menu bar
        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(searchAction(this::onSearch))
                ),
                newMenu("Вид",
                        menuItem("Сбросить фильтр", SHORTCUT_ALT_C, event -> resetFilter())),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context menu
        table.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableObjectColumn("Имя", b ->
                        b.withCellFactory(x -> new ContactNameCell())
                                .withComparator(Comparator.comparing(Contact::name))
                                .withWidthBinding(w.multiply(0.4))),
                tableColumn("Тип", b ->
                        b.withPropertyCallback((Contact p) -> translate(p.type())).withWidthBinding(w.multiply(0.2))),
                tableColumn("Телефон", b ->
                        b.withPropertyCallback(Contact::phone).withWidthBinding(w.multiply(0.2))),
                tableColumn("E-Mail", b ->
                        b.withPropertyCallback(Contact::email).withWidthBinding(w.multiply(0.2)))
        ));
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.getSortOrder().add(table.getColumns().get(NAME_COLUMN_INDEX));
        table.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        var hBox = hBox(5, searchField, typeBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var self = new BorderPane(
                new BorderPane(table, hBox, null, null, null),
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
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return "Контакты";
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

    private void onCreateContact(ActionEvent event) {
        new ContactDialog(this, settings().getDialogCssFileUrl(), null, cache()).showAndWait()
                .ifPresent(c -> dao().insertContact(c));
    }

    private void onEditContact(ActionEvent event) {
        getSelectedContact()
                .flatMap(selected ->
                        new ContactDialog(this, settings().getDialogCssFileUrl(), selected, cache()).showAndWait())
                .ifPresent(c -> dao().updateContact(c));
    }

    private void onTableMouseClick(Event event) {
        if (((MouseEvent) event).getClickCount() == 2) {
            onEditContact(null);
        }
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }

    private void resetFilter() {
        clearValueAndSelection(typeBox);
        searchField.setText("");
    }

    private void onSearch(ActionEvent ignored) {
        searchField.requestFocus();
    }
}
