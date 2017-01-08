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

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.utilities.fx.Controller;

public class CategoryWindowController extends Controller implements Initializable {
    @FXML private final ObservableList<Category> categoryList = FXCollections.observableArrayList();

    @FXML private TableView<Category> categoryTable;
    @FXML private TableColumn<Category,String> colType;
    @FXML private TableColumn<Category,String> colName;
    @FXML private TableColumn<Category,String> colDescription;

    @FXML private MenuItem editMenuItem;
    @FXML private MenuItem ctxEditMenuItem;

    @FXML private Parent self;

    private ResourceBundle bundle;

    private final MoneyDAO dao;

    public CategoryWindowController() {
        super("/org/panteleyev/money/CategoryWindow.fxml", MainWindowController.UI_BUNDLE_PATH, true);

        dao = MoneyDAO.getInstance();
    }

    public void onClose() {
        ((Stage)(self.getScene().getWindow())).close();
    }

    @Override
    public String getTitle() {
        return bundle == null? "Categories" : bundle.getString("category.Window.Title");
    }

    private void updateList() {
        categoryList.clear();
        Collection<Category> categories = dao.getCategories();
        if (categories != null) {
            categoryList.addAll(categories);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;

        categoryTable.setItems(categoryList);
        updateList();

        colType.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) ->
                new ReadOnlyObjectWrapper(dao.getCategoryType(p.getValue().getCatTypeId())
                    .map(CategoryType::getTranslatedName)
                    .orElse("")));
        colName.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) -> new ReadOnlyObjectWrapper(p.getValue().getName()));
        colDescription.setCellValueFactory((TableColumn.CellDataFeatures<Category, String> p) -> new ReadOnlyObjectWrapper(p.getValue().getComment()));

        colType.setSortable(true);
        categoryTable.getSortOrder().addAll(colType);
        colType.setSortType(TableColumn.SortType.ASCENDING);

        editMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty().bind(categoryTable.getSelectionModel().selectedItemProperty().isNull());

        colType.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colName.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.2));
        colDescription.prefWidthProperty().bind(categoryTable.widthProperty().subtract(20).multiply(0.6));
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
}
