/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.category;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
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
import org.panteleyev.fx.FxFactory;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxUtils.fxNode;
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
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class CategoryWindowController extends BaseController {
    private final ComboBox<CategoryType> typeBox = comboBox(CategoryType.values(),
            b -> b.withDefaultString("Все типы")
                    .withStringConverter(Bundles::translate)
    );
    private final TextField searchField = FxFactory.searchField(SEARCH_FIELD_FACTORY, _ -> updatePredicate());

    private final FilteredList<Category> filteredList = cache().getCategories().filtered(_ -> true);
    private final TableView<Category> tableView = new CategoryTableView(filteredList.sorted());

    public CategoryWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCategory, this::onEditCategory, _ -> {},
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );

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

        // Context Menu
        tableView.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction())
        ));

        tableView.setOnMouseClicked(this::onTableMouseClick);

        var pane = new BorderPane(tableView,
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
