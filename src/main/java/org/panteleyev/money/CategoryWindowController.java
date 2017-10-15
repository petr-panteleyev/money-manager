/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.persistence.Category;
import java.util.Optional;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class CategoryWindowController extends BaseController {
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private TableView<Category> categoryTable = new TableView<>();

    private BorderPane self = new BorderPane();

    private final MapChangeListener<Integer, Category> categoriesListener = change ->
            Platform.runLater(this::updateWindow);


    CategoryWindowController() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = event -> onMenuAdd();
        EventHandler<ActionEvent> editHandler = event -> onMenuEdit();

        // Main Menu
        MenuItem closeMenuItem = new MenuItem(RB.getString("menu.File.Close"));
        closeMenuItem.setOnAction(event -> onClose());
        Menu fileMenu = new Menu(RB.getString("menu.File"), null, closeMenuItem);

        MenuItem addMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        addMenuItem.setOnAction(addHandler);
        MenuItem editMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        editMenuItem.setOnAction(editHandler);
        Menu editMenu = new Menu(RB.getString("menu.Edit"), null, addMenuItem, editMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, createHelpMenu(RB));
        menuBar.setUseSystemMenuBar(true);

        // Context Menu
        MenuItem ctxAddMenuItem = new MenuItem(RB.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);
        MenuItem ctxEditMenuItem = new MenuItem(RB.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);
        categoryTable.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        TableColumn<Category, String> colType = new TableColumn<>(RB.getString("column.Type"));
        TableColumn<Category, String> colName = new TableColumn<>(RB.getString("column.Name"));
        TableColumn<Category, String> colDescription = new TableColumn<>(RB.getString("column.Description"));

        categoryTable.getColumns().setAll(colType, colName, colDescription);

        categoryTable.setOnMouseClicked(this::onTableMouseClick);

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

        editMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());

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
        MouseEvent me = (MouseEvent) event;
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
                getDao().insertCategory(c.copy(getDao().generatePrimaryKey(Category.class)));
            }
        });
    }

    private void updateWindow() {
        int selIndex = categoryTable.getSelectionModel().getSelectedIndex();
        categoryList.setAll(getDao().categories().values());
        categoryTable.getSelectionModel().select(selIndex);
    }
}
