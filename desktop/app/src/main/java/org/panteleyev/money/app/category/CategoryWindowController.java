// Copyright © 2017-2025 Petr Panteleyev
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

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.functional.Scope.apply;
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
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

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
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        tableView.setOnMouseClicked(this::onTableMouseClick);

        var pane = new BorderPane(tableView,
                apply(
                        hBox(List.of(searchField, typeBox)),
                        box -> {
                            box.setSpacing(BIG_SPACING);
                            box.setAlignment(Pos.CENTER_LEFT);
                            BorderPane.setMargin(box, BIG_INSETS);
                        }
                ),
                null, null, null);

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
        return menuBar(
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
                        apply(menuItem("Сбросить фильтр"), menuItem -> {
                            menuItem.setAccelerator(SHORTCUT_ALT_C);
                            menuItem.setOnAction(_ -> resetFilter());
                        }),
                        createWindowMenu(),
                        createHelpMenu()
                )
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
