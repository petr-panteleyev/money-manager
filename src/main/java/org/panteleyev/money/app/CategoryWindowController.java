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
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.cells.CategoryNameCell;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.FxUtils.fxNode;
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
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CategoryWindowController extends BaseController {
    private final ComboBox<CategoryType> typeBox = comboBox(CategoryType.values(),
        b -> b.withDefaultString(ALL_TYPES_STRING));
    private final TextField searchField = newSearchField(SEARCH_FIELD_FACTORY, s -> updatePredicate());

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

        var menuBar = menuBar(
            newMenu(fxString(RB, "File"),
                menuItem(fxString(RB, "Close"), event -> onClose())),
            newMenu(fxString(RB, "menu.Edit"),
                menuItem(fxString(RB, "Create"), SHORTCUT_N, addHandler),
                menuItem(fxString(RB, "menu.Edit.Edit"), SHORTCUT_E, editHandler, disableBinding),
                new SeparatorMenuItem(),
                menuItem(fxString(RB, "menu.Edit.Search"), SHORTCUT_F, actionEvent -> searchField.requestFocus())),
            newMenu(fxString(RB, "View"),
                menuItem(fxString(RB, "Reset_Filter"), SHORTCUT_ALT_C, event -> resetFilter())),
            createWindowMenu(),
            createHelpMenu());

        // Context Menu
        categoryTable.setContextMenu(new ContextMenu(
            menuItem(fxString(RB, "Create"), addHandler),
            menuItem(fxString(RB, "menu.Edit.Edit"), editHandler, disableBinding)));

        // Table
        var w = categoryTable.widthProperty().subtract(20);
        categoryTable.getColumns().setAll(List.of(
            tableColumn(fxString(RB, "Type"),
                b -> b.withPropertyCallback(c -> c.type().toString()).withWidthBinding(w.multiply(0.2))),
            tableObjectColumn(fxString(RB, "column.Name"),
                b -> b.withCellFactory(x -> new CategoryNameCell()).withWidthBinding(w.multiply(0.4))),
            tableColumn(fxString(RB, "Comment"),
                b -> b.withPropertyCallback(Category::comment).withWidthBinding(w.multiply(0.4)))
        ));

        categoryTable.setOnMouseClicked(this::onTableMouseClick);

        var pane = new BorderPane(categoryTable,
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
        Platform.runLater(this::resetFilter);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(categoryTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("Categories");
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
        var me = (MouseEvent) event;
        if (me.getClickCount() == 2) {
            onMenuEdit();
        }
    }

    private void onMenuEdit() {
        getSelectedCategory()
            .flatMap(category ->
                new CategoryDialog(this, options().getDialogCssFileUrl(), category).showAndWait())
            .ifPresent(c -> getDao().updateCategory(c));
    }

    private void onMenuAdd() {
        new CategoryDialog(this, options().getDialogCssFileUrl(), null).showAndWait()
            .ifPresent(c -> getDao().insertCategory(c));
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }

    private void resetFilter() {
        clearValueAndSelection(typeBox);
    }
}
