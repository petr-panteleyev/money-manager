package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.icons.IconManager;
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
