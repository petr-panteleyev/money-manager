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
import org.panteleyev.money.app.cells.CategoryNameCell;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.Arrays;
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

final class CategoryWindowController extends BaseController {
    private final ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private final TextField searchField = newSearchField(Images.SEARCH, s -> updatePredicate());

    private final FilteredList<Category> filteredList = cache().getCategories().filtered(x -> true);
    private final SortedList<Category> sortedList = filteredList.sorted(
        MoneyDAO.COMPARE_CATEGORY_BY_TYPE.thenComparing(MoneyDAO.COMPARE_CATEGORY_BY_NAME)
    );
    private final TableView<Category> categoryTable = new TableView<>(sortedList);

    CategoryWindowController() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = event -> onMenuAdd();
        EventHandler<ActionEvent> editHandler = event -> onMenuEdit();

        var disableBinding = categoryTable.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "Create", SHORTCUT_N, addHandler),
                newMenuItem(RB, "menu.Edit.Edit", SHORTCUT_E, editHandler, disableBinding),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.Edit.Search", SHORTCUT_F, actionEvent -> searchField.requestFocus())),
            createWindowMenu(),
            createHelpMenu());

        // Context Menu
        categoryTable.setContextMenu(new ContextMenu(
            newMenuItem(RB, "Create", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var w = categoryTable.widthProperty().subtract(20);
        categoryTable.getColumns().setAll(List.of(
            newTableColumn(RB, "Type", null, c -> c.type().getTypeName(), w.multiply(0.2)),
            newTableColumn(RB, "column.Name", x -> new CategoryNameCell(), w.multiply(0.4)),
            newTableColumn(RB, "Comment", null, Category::comment, w.multiply(0.4))
        ));

        categoryTable.setOnMouseClicked(this::onTableMouseClick);

        var hBox = new HBox(5, searchField, typeChoiceBox);
        var pane = new BorderPane();
        pane.setTop(hBox);
        pane.setCenter(categoryTable);

        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        var self = new BorderPane();
        self.setPrefSize(600.0, 400.0);
        self.setTop(menuBar);
        self.setCenter(pane);

        typeChoiceBox.getItems().add(RB.getString("All_Types"));
        typeChoiceBox.getItems().add(new Separator());
        typeChoiceBox.getItems().addAll(Arrays.asList(CategoryType.values()));
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                return obj instanceof CategoryType type ? type.getTypeName() : obj.toString();
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());
        setupWindow(self);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(categoryTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("Categories");
    }

    private Predicate<Category> getPredicate() {
        Predicate<Category> filter;

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

    private void onTableMouseClick(Event event) {
        var me = (MouseEvent) event;
        if (me.getClickCount() == 2) {
            onMenuEdit();
        }
    }

    private void onMenuEdit() {
        getSelectedCategory().flatMap(category ->
            new CategoryDialog(this, category).showAndWait()).ifPresent(c -> getDao().updateCategory(c));
    }

    private void onMenuAdd() {
        new CategoryDialog(this, null).showAndWait().ifPresent(c -> getDao().insertCategory(c));
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }
}
