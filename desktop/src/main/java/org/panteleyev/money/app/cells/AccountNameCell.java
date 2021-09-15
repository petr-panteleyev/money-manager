/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;

public class AccountNameCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        if (empty || account == null) {
            setText("");
            setGraphic(null);
        } else {
            setText(account.name());
            setGraphic(IconManager.getImageView(account.iconUuid()));
        }
    }
}
