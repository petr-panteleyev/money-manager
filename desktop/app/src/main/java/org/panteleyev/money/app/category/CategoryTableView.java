/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.category;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.category.cells.CategoryNameCell;
import org.panteleyev.money.app.category.cells.CategoryTypeCell;
import org.panteleyev.money.model.Category;

import java.util.List;

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;

final class CategoryTableView extends TableView<Category> {
    public CategoryTableView(SortedList<Category> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                tableObjectColumn("Тип",
                        b -> b.withCellFactory(_ -> new CategoryTypeCell())
                                .withComparator(Comparators.categoriesByType()
                                        .thenComparing(Comparators.categoriesByName()))
                                .withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("Название",
                        b -> b.withCellFactory(_ -> new CategoryNameCell())
                                .withComparator(Comparators.categoriesByName())
                                .withWidthBinding(w.multiply(0.4))),
                tableColumn("Комментарий",
                        b -> b.withPropertyCallback(Category::comment).withWidthBinding(w.multiply(0.4)))
        ));

        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(getColumns().getFirst());
        sort();
    }
}
