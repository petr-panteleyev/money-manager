/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import org.panteleyev.money.persistence.MoneyDAO;
import java.util.Collection;
import java.util.ResourceBundle;

public class CategoryWindowController extends BaseController {
    private final ResourceBundle rb = ResourceBundle.getBundle(MainWindowController.UI_BUNDLE_PATH);

    private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    private final TableView<Category> categoryTable = new TableView<>();

    private final BorderPane self = new BorderPane();

    private final MapChangeListener<Integer,Category> categoriesListener =
            (MapChangeListener<Integer,Category>)l -> Platform.runLater(this::updateWindow);

    CategoryWindowController() {
        super(null);
        initialize();
        setupWindow(self);
    }

    @Override
    public String getTitle() {
        return rb.getString("category.Window.Title");
    }

    private void updateList() {
        categoryList.clear();
        Collection<Category> categories = MoneyDAO.getInstance().getCategories();
        if (categories != null) {
            categoryList.addAll(categories);
        }
    }

    private void initialize() {
        // Event handlers
        EventHandler<ActionEvent> addHandler = this::onMenuAdd;
        EventHandler<ActionEvent> editHandler = this::onMenuEdit;

        // Main Menu
        MenuItem closeMenuItem = new MenuItem(rb.getString("menu.File.Close"));
        closeMenuItem.setOnAction(ACTION_FILE_CLOSE);
        Menu fileMenu = new Menu(rb.getString("menu.File"), null, closeMenuItem);

        MenuItem addMenuItem = new MenuItem(rb.getString("menu.Edit.Add"));
        addMenuItem.setOnAction(addHandler);
        MenuItem editMenuItem = new MenuItem(rb.getString("menu.Edit.Edit"));
        editMenuItem.setOnAction(editHandler);
        Menu editMenu = new Menu(rb.getString("menu.Edit"), null, addMenuItem, editMenuItem);

        MenuBar menuBar = new MenuBar(fileMenu, editMenu, createHelpMenu(rb));
        menuBar.setUseSystemMenuBar(true);

        // Context Menu
        MenuItem ctxAddMenuItem = new MenuItem(rb.getString("menu.Edit.Add"));
        ctxAddMenuItem.setOnAction(addHandler);
        MenuItem ctxEditMenuItem = new MenuItem(rb.getString("menu.Edit.Edit"));
        ctxEditMenuItem.setOnAction(editHandler);
        categoryTable.setContextMenu(new ContextMenu(ctxAddMenuItem, ctxEditMenuItem));

        // Table
        TableColumn<Category,String> colType = new TableColumn<>(rb.getString("column.Type"));
        TableColumn<Category,String> colName = new TableColumn<>(rb.getString("column.Name"));
        TableColumn<Category,String> colDescription = new TableColumn<>(rb.getString("column.Description"));

        categoryTable.getColumns().setAll(colType, colName, colDescription);

        categoryTable.setOnMouseClicked(this::onTableMouseClick);

        self.setPrefSize(600, 400);
        self.setTop(menuBar);
        self.setCenter(categoryTable);

        categoryTable.setItems(categoryList);
        updateList();

        colType.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper<>(p.getValue().getType().getName()));
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

        MoneyDAO.getInstance().categories()
                .addListener(new WeakMapChangeListener<>(categoriesListener));
    }

    private void onTableMouseClick(Event event) {
        MouseEvent me = (MouseEvent)event;
        if (me.getClickCount() == 2) {
            Category category = categoryTable.getSelectionModel().getSelectedItem();
            if (category != null) {
                openCategoryDialog(category);
            }
        }
    }

    private void onMenuEdit(ActionEvent evt) {
        Category category = categoryTable.getSelectionModel().getSelectedItem();
        if (category != null) {
            openCategoryDialog(category);
        }
    }

    private void onMenuAdd(ActionEvent evt) {
        openCategoryDialog(null);
    }

    private void openCategoryDialog(Category category) {
        MoneyDAO dao = MoneyDAO.getInstance();
        new CategoryDialog(category).showAndWait().ifPresent(builder -> {
            if (builder.id() != 0) {
                dao.updateCategory(builder.build());
            } else {
                dao.insertCategory(builder
                        .id(dao.generatePrimaryKey(Category.class))
                        .build());
            }
        });
    }

    private void updateWindow() {
        int selIndex = categoryTable.getSelectionModel().getSelectedIndex();
        categoryList.setAll(MoneyDAO.getInstance().categories().values());
        categoryTable.getSelectionModel().select(selIndex);
    }
}
