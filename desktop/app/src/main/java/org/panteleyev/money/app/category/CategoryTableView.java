// Copyright © 2023-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.category;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.category.cells.CategoryNameCell;
import org.panteleyev.money.app.category.cells.CategoryTypeCell;
import org.panteleyev.money.model.Category;

import java.util.List;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableObjectColumn;
import static org.panteleyev.fx.factories.TableFactory.tableStringColumn;

final class CategoryTableView extends TableView<Category> {
    public CategoryTableView(SortedList<Category> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                apply(tableObjectColumn("Тип"), c -> {
                    c.setCellFactory(_ -> new CategoryTypeCell());
                    c.comparator(Comparators.categoriesByType().thenComparing(Comparators.categoriesByName()));
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableObjectColumn("Название"), c -> {
                    c.setCellFactory(_ -> new CategoryNameCell());
                    c.comparator(Comparators.categoriesByName());
                    c.widthBinding(w.multiply(0.4));
                }),
                apply(tableStringColumn("Комментарий"), c -> {
                    c.valueConverter(Category::comment);
                    c.widthBinding(w.multiply(0.4));
                })
        ));

        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(getColumns().getFirst());
        sort();
    }
}
