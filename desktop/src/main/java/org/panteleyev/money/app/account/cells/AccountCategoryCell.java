/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;

import static org.panteleyev.money.app.GlobalContext.cache;

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
