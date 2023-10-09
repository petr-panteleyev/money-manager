/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.category;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.category.cells.CategoryNameCell;
import org.panteleyev.money.app.category.cells.CategoryTypeCell;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.clearValueAndSelection;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_C;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class CategoryWindowController extends BaseController {
    private final ComboBox<CategoryType> typeBox = comboBox(CategoryType.values(),
            b -> b.withDefaultString("Все типы")
                    .withStringConverter(Bundles::translate)
    );
    private final TextField searchField = newSearchField(SEARCH_FIELD_FACTORY, s -> updatePredicate());

    private final FilteredList<Category> filteredList = cache().getCategories().filtered(x -> true);
    private final SortedList<Category> sortedList = filteredList.sorted();
    private final TableView<Category> table = new TableView<>(sortedList);

    public CategoryWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCategory, this::onEditCategory, e -> {},
                table.getSelectionModel().selectedItemProperty().isNull()
        );

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

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        // Table
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableObjectColumn("Тип",
                        b -> b.withCellFactory(x -> new CategoryTypeCell())
                                .withComparator(Comparators.categoriesByType()
                                        .thenComparing(Comparators.categoriesByName()))
                                .withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("Название",
                        b -> b.withCellFactory(x -> new CategoryNameCell())
                                .withComparator(Comparators.categoriesByName())
                                .withWidthBinding(w.multiply(0.4))),
                tableColumn("Комментарий",
                        b -> b.withPropertyCallback(Category::comment).withWidthBinding(w.multiply(0.4)))
        ));
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.getSortOrder().add(table.getColumns().get(0));

        table.setOnMouseClicked(this::onTableMouseClick);

        var pane = new BorderPane(table,
                fxNode(
                        hBox(List.of(searchField, typeBox), b -> {
                            b.setSpacing(BIG_SPACING);
                            b.setAlignment(Pos.CENTER_LEFT);
                        }),
                        b -> BorderPane.setMargin(b, new Insets(5.0, 5.0, 5.0, 5.0))
                ),
                null, null, null);

        var self = new BorderPane(pane, menuBar, null, null, null);
        self.setPrefSize(600.0, 400.0);

        typeBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        setupWindow(self);
        settings().loadStageDimensions(this);

        Platform.runLater(this::resetFilter);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return "Категории";
    }

    private Predicate<Category> getPredicate() {
        // Type
        var type = typeBox.getSelectionModel().getSelectedItem();
        Predicate<Category> filter = type == null ? x -> true : x -> x.type() == type;

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
