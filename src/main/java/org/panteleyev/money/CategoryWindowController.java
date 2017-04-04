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
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.MoneyDAO;
import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

public class CategoryWindowController extends BaseController implements Initializable {
    @FXML private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category,String> colType;
    @FXML private TableColumn<Category,String> colName;
    @FXML private TableColumn<Category,String> colDescription;

    @FXML private MenuBar  menuBar;
    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem ctxEditMenuItem;

    @FXML private Parent self;

    private ResourceBundle bundle;

    private final MapChangeListener<Integer,Category> categoriesListener =
            (MapChangeListener<Integer,Category>)l -> Platform.runLater(this::updateWindow);

    public CategoryWindowController() {
        super("/org/panteleyev/money/CategoryWindow.fxml", MainWindowController.UI_BUNDLE_PATH, true);
    }

    @Override
    protected Parent getSelf() {
        return self;
    }

    @Override
    public String getTitle() {
        return bundle == null? "Categories" : bundle.getString("category.Window.Title");
    }

    private void updateList() {
        categoryList.clear();
        Collection<Category> categories = MoneyDAO.getInstance().getCategories();
        if (categories != null) {
            categoryList.addAll(categories);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;

        menuBar.setUseSystemMenuBar(true);

        categoryTable.setItems(categoryList);
        updateList();

        colType.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getCatType().getName()));
        colName.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getName()));
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getComment()));

        colType.setSortable(true);
        categoryTable.getSortOrder().addAll(colType);
        colType.setSortType(TableColumn.SortType.ASCENDING);

        editMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());

        colType.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colName.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colDescription.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.6));

        MoneyDAO.getInstance().categoriesProperty()
                .addListener(new WeakMapChangeListener<>(categoriesListener));
    }

    public void onTableMouseClick(Event event) {
        MouseEvent me = (MouseEvent)event;
        if (me.getClickCount() == 2) {
            Category category = categoryTable.getSelectionModel().getSelectedItem();
            if (category != null) {
                openCategoryDialog(category);
            }
        }
    }

    public void onMenuEdit() {
        Category category = categoryTable.getSelectionModel().getSelectedItem();
        if (category != null) {
            openCategoryDialog(category);
        }
    }

    public void onMenuAdd() {
        openCategoryDialog(null);
    }

    private void openCategoryDialog(Category category) {
        MoneyDAO dao = MoneyDAO.getInstance();
        new CategoryDialog(category).load().showAndWait().ifPresent(builder -> {
            if (builder.id().isPresent()) {
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
        categoryList.setAll(MoneyDAO.getInstance().categoriesProperty().values());
        categoryTable.getSelectionModel().select(selIndex);
    }
}
