package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.cells.CategoryNameCell;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CategoryWindowController extends BaseController {
    private final ChoiceBox<Object> typeChoiceBox = new ChoiceBox<>();
    private final TextField searchField = newSearchField(Images.SEARCH, s -> updatePredicate());

    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final FilteredList<Category> filteredList = new FilteredList<>(categoryList);
    private final TableView<Category> categoryTable = new TableView<>(filteredList);

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Category> categoriesListener = change ->
        Platform.runLater(this::updateWindow);

    CategoryWindowController() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = event -> onMenuAdd();
        EventHandler<ActionEvent> editHandler = event -> onMenuEdit();

        var disableBinding = categoryTable.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Close", event -> onClose())),
            newMenu(RB, "menu.Edit",
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding),
                new SeparatorMenuItem(),
                newMenuItem(RB, "menu.Edit.Search",
                    new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                    actionEvent -> searchField.requestFocus())),
            createWindowMenu(RB),
            createHelpMenu(RB));

        // Context Menu
        categoryTable.setContextMenu(new ContextMenu(
            newMenuItem(RB, "menu.Edit.Add", addHandler),
            newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var w = categoryTable.widthProperty().subtract(20);
        categoryTable.getColumns().setAll(List.of(
            newTableColumn(RB, "Type", null, c -> c.getType().getTypeName(), w.multiply(0.2)),
            newTableColumn(RB, "column.Name", x -> new CategoryNameCell(), w.multiply(0.4)),
            newTableColumn(RB, "Comment", null, Category::getComment, w.multiply(0.4))
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
        typeChoiceBox.getItems().addAll(CategoryType.values());
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                return obj instanceof CategoryType ? ((CategoryType) obj).getTypeName() : obj.toString();
            }
        });

        typeChoiceBox.valueProperty().addListener((x, y, newValue) -> updatePredicate());

        updateList();

        cache().categories().addListener(new WeakMapChangeListener<>(categoriesListener));
        setupWindow(self);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(categoryTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("Categories");
    }

    private void updateList() {
        categoryList.setAll(cache().categories().values().stream()
            .sorted(MoneyDAO.COMPARE_CATEGORY_BY_TYPE.thenComparing(MoneyDAO.COMPARE_CATEGORY_BY_NAME))
            .collect(Collectors.toList()));
        updatePredicate();
    }

    private Predicate<Category> getPredicate() {
        Predicate<Category> filter;

        // Type
        var type = typeChoiceBox.getSelectionModel().getSelectedItem();
        if (type instanceof String) {
            filter = x -> true;
        } else {
            filter = x -> x.getType() == type;
        }

        // Name
        var search = searchField.getText().toLowerCase();
        if (!search.isEmpty()) {
            filter = filter.and(x -> x.getName().toLowerCase().contains(search));
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

    private void updateWindow() {
        int selIndex = categoryTable.getSelectionModel().getSelectedIndex();
        updateList();
        categoryTable.getSelectionModel().select(selIndex);
    }

    private void updatePredicate() {
        filteredList.setPredicate(getPredicate());
    }
}
