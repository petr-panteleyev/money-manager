/*
 Copyright © 2019-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.category.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Category;

import static org.panteleyev.money.app.Bundles.translate;

public class CategoryTypeCell extends TableCell<Category, Category> {
    @Override
    protected void updateItem(Category category, boolean empty) {
        super.updateItem(category, empty);

        if (empty || category == null) {
            setText("");
        } else {
            setText(translate(category.type()));
        }
    }
}
