// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.category;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.category.cells.CategoryNameCell;
import org.panteleyev.money.app.category.cells.CategoryTypeCell;
import org.panteleyev.money.model.Category;

import java.util.List;

final class CategoryTableView extends TableView<Category> {
    public CategoryTableView(SortedList<Category> list) {
        super(list);

        var w = widthProperty().subtract(20);

        var typeColumn = TableFactory.<Category>tableObjectColumn("Тип");
        typeColumn.setCellFactory(_ -> new CategoryTypeCell());
        typeColumn.comparator(Comparators.categoriesByType().thenComparing(Comparators.categoriesByName()));
        typeColumn.widthBinding(w.multiply(0.2));

        var nameColumn = TableFactory.<Category>tableObjectColumn("Название");
        nameColumn.setCellFactory(_ -> new CategoryNameCell());
        nameColumn.comparator(Comparators.categoriesByName());
        nameColumn.widthBinding(w.multiply(0.4));

        var commentColumn = TableFactory.<Category>tableStringColumn("Комментарий");
        commentColumn.valueConverter(Category::comment);
        commentColumn.widthBinding(w.multiply(0.4));

        getColumns().setAll(List.of(typeColumn, nameColumn, commentColumn));

        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(typeColumn);
        sort();
    }
}
