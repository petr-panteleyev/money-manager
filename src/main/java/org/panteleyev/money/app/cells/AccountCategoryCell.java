/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;
import static org.panteleyev.money.persistence.DataCache.cache;

public class AccountCategoryCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account item, boolean empty) {
        super.updateItem(item, empty);

        setText("");
        setGraphic(null);

        if (empty || item == null) {
            return;
        }

        cache().getCategory(item.categoryUuid()).ifPresent(category -> {
            setText(category.name());
            setGraphic(IconManager.getImageView(category.iconUuid()));
        });
    }
}
