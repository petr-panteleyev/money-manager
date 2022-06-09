/*
 Copyright © 2019-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Category;

public class CategoryNameCell extends TableCell<Category, Category> {
    @Override
    protected void updateItem(Category category, boolean empty) {
        super.updateItem(category, empty);

        if (empty || category == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(category.name());
            setGraphic(IconManager.getImageView(category.iconUuid()));
        }
    }
}
