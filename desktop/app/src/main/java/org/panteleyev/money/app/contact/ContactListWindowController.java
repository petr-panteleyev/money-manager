/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.contact;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.FxFactory;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;

import java.util.Optional;
import java.util.function.Predicate;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class ContactListWindowController extends BaseController {
    private final ComboBox<ContactType> typeBox = comboBox(ContactType.values(),
            b -> b.withDefaultString("Все типы")
                    .withStringConverter(Bundles::translate)
    );
    private final TextField searchField = FxFactory.searchField(SEARCH_FIELD_FACTORY, _ -> updatePredicate());

    private final FilteredList<Contact> filteredList = cache().getContacts().filtered(_ -> true);
    private final TableView<Contact> tableView = new ContactTableView(filteredList.sorted());

    public ContactListWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateContact, this::onEditContact, _ -> {},
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );

        // Menu bar
        var menuBar = menuBar(
                menu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                menu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(searchAction(this::onSearch))
                ),
                menu("Вид",
                        menuItem("Сбросить фильтр", SHORTCUT_ALT_C, _ -> resetFilter())),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        // Context menu
        tableView.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));
        tableView.setOnMouseClicked(this::onTableMouseClick);

        // Toolbox
        var hBox = hBox(5, searchField, typeBox);
        BorderPane.setMargin(hBox, BIG_INSETS);

        var self = new BorderPane(
                new BorderPane(tableView, hBox, null, null, null),
                menuBar, null, null, null
        );
        self.setPrefSize(600.0, 400.0);

        typeBox.getSelectionModel().select(0);

        typeBox.valueProperty().addListener((_, _, _) -> updatePredicate());

        reloadContacts();
        setupWindow(self);
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Contact> getSelectedContact() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
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
        Predicate<Contact> filter = type == null ? _ -> true : x -> x.type() == type;

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
