/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.model.Category;
import java.util.Optional;
import static org.panteleyev.money.FXFactory.newMenuBar;
import static org.panteleyev.money.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.dto.Dto.dtoClass;

final class CategoryWindowController extends BaseController {
    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();
    private final TableView<Category> categoryTable = new TableView<>();

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<Integer, Category> categoriesListener = change ->
            Platform.runLater(this::updateWindow);


    CategoryWindowController() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = event -> onMenuAdd();
        EventHandler<ActionEvent> editHandler = event -> onMenuEdit();

        var disableBinding = categoryTable.getSelectionModel().selectedItemProperty().isNull();

        var menuBar = newMenuBar(
                new Menu(RB.getString("menu.File"), null,
                        newMenuItem(RB, "menu.File.Close", event -> onClose())),
                new Menu(RB.getString("menu.Edit"), null,
                        newMenuItem(RB, "menu.Edit.Add", addHandler),
                        newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)),
                createHelpMenu(RB));

        // Context Menu
        categoryTable.setContextMenu(new ContextMenu(
                newMenuItem(RB, "menu.Edit.Add", addHandler),
                newMenuItem(RB, "menu.Edit.Edit", editHandler, disableBinding)));

        // Table
        var colType = new TableColumn<Category, String>(RB.getString("column.Type"));
        var colName = new TableColumn<Category, String>(RB.getString("column.Name"));
        var colDescription = new TableColumn<Category, String>(RB.getString("column.Description"));

        //noinspection unchecked
        categoryTable.getColumns().setAll(colType, colName, colDescription);

        categoryTable.setOnMouseClicked(this::onTableMouseClick);

        var self = new BorderPane();
        self.setPrefSize(600.0, 400.0);
        self.setTop(menuBar);
        self.setCenter(categoryTable);

        categoryTable.setItems(categoryList);
        updateList();

        colType.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getType().getTypeName()));
        colName.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getName()));
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getComment()));

        colType.setSortable(true);
        categoryTable.getSortOrder().add(colType);
        colType.setSortType(TableColumn.SortType.ASCENDING);

        colType.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colName.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colDescription.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.6));

        getDao().categories().addListener(new WeakMapChangeListener<>(categoriesListener));
        setupWindow(self);
    }

    private Optional<Category> getSelectedCategory() {
        return Optional.ofNullable(categoryTable.getSelectionModel().getSelectedItem());
    }

    @Override
    public String getTitle() {
        return RB.getString("category.Window.Title");
    }

    private void updateList() {
        categoryList.setAll(getDao().getCategories());
    }

    private void onTableMouseClick(Event event) {
        var me = (MouseEvent) event;
        if (me.getClickCount() == 2) {
            getSelectedCategory().ifPresent(this::openCategoryDialog);
        }
    }

    private void onMenuEdit() {
        getSelectedCategory().ifPresent(this::openCategoryDialog);
    }

    private void onMenuAdd() {
        openCategoryDialog(null);
    }

    private void openCategoryDialog(Category category) {
        new CategoryDialog(category).showAndWait().ifPresent(c -> {
            if (c.getId() != 0) {
                getDao().updateCategory(c);
            } else {
                getDao().insertCategory(c.copy(getDao().generatePrimaryKey(dtoClass(Category.class))));
            }
        });
    }

    private void updateWindow() {
        int selIndex = categoryTable.getSelectionModel().getSelectedIndex();
        categoryList.setAll(getDao().categories().values());
        categoryTable.getSelectionModel().select(selIndex);
    }
}
