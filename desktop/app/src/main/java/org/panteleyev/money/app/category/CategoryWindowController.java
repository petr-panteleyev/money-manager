// Copyright © 2017-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.category;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.factories.TextFieldFactory;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.panteleyev.fx.factories.BoxFactory.hBox;
import static org.panteleyev.fx.factories.ComboBoxFactory.clearValueAndSelection;
import static org.panteleyev.fx.factories.ComboBoxFactory.comboBox;
import static org.panteleyev.fx.factories.ComboBoxFactory.comboBoxListCell;
import static org.panteleyev.fx.factories.MenuFactory.menu;
import static org.panteleyev.fx.factories.MenuFactory.menuBar;
import static org.panteleyev.fx.factories.MenuFactory.menuItem;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.Styles.BIG_SPACING;

public final class CategoryWindowController extends BaseController {
    private final ComboBox<CategoryType> typeBox = comboBox(CategoryType.asList(),
            _ -> comboBoxListCell("Все типы", Bundles::translate));
    private final TextField searchField = TextFieldFactory.searchField(SEARCH_FIELD_FACTORY, _ -> updatePredicate());

    private final FilteredList<Category> filteredList = cache().getCategories().filtered(_ -> true);
    private final TableView<Category> tableView = new CategoryTableView(filteredList.sorted());
    private final CrudActionsHolder crudActionsHolder = new CrudActionsHolder(
            this::onCreateCategory, this::onEditCategory, _ -> {},
            tableView.getSelectionModel().selectedItemProperty().isNull()
    );

    public CategoryWindowController() {
        // Context Menu
        tableView.setContextMenu(new ContextMenu(
                crudActionsHolder.getCreateAction().createMenuItem(),
                crudActionsHolder.getUpdateAction().createMenuItem()
        ));

        tableView.setOnMouseClicked(this::onTableMouseClick);

        var toolBar = hBox(List.of(searchField, typeBox));
        toolBar.setSpacing(BIG_SPACING);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(toolBar, BIG_INSETS);

        var pane = new BorderPane(tableView, toolBar, null, null, null);
        var self = new BorderPane(pane, createMenuBar(), null, null, null);
        self.setPrefSize(600.0, 400.0);

        typeBox.valueProperty().addListener((_, _, _) -> updatePredicate());

        setupWindow(self);
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return "Категории";
    }

    private MenuBar createMenuBar() {
        var resetFilterMenuItem = menuItem("Сбросить фильтр", _ -> resetFilter());
        resetFilterMenuItem.setAccelerator(SHORTCUT_ALT_C);

        return menuBar(
                menu("Файл", ACTION_CLOSE.createMenuItem()),
                menu("Правка",
                        crudActionsHolder.getCreateAction().createMenuItem(),
                        crudActionsHolder.getUpdateAction().createMenuItem(),
                        new SeparatorMenuItem(),
                        searchAction(this::onSearch).createMenuItem()
                ),
                menu("Вид", resetFilterMenuItem),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private Predicate<Category> getPredicate() {
        // Type
        var type = typeBox.getSelectionModel().getSelectedItem();
        Predicate<Category> filter = type == null ? _ -> true : x -> x.type() == type;

        // Name
        var search = searchField.getText().toLowerCase();
        if (!search.isEmpty()) {
            filter = filter.and(x -> x.name().toLowerCase().contains(search));
        }

        return filter;
    }

    private void onTableMouseClick(Event event) {
        if (event instanceof MouseEvent mouseEvent && mouseEvent.getClickCount() == 2) {
            onEditCategory(null);
        }
    }

    private void onEditCategory(ActionEvent event) {
        getSelectedCategory()
                .flatMap(category ->
                        new CategoryDialog(this, settings().getDialogCssFileUrl(), category).showAndWait())
                .ifPresent(c -> dao().updateCategory(c));
    }

    private void onCreateCategory(ActionEvent event) {
        new CategoryDialog(this, settings().getDialogCssFileUrl(), null).showAndWait()
                .ifPresent(c -> dao().insertCategory(c));
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }

    private void resetFilter() {
        clearValueAndSelection(typeBox);
    }

    private void onSearch(ActionEvent ignored) {
        searchField.requestFocus();
    }
}
