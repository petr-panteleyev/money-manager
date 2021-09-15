/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;

public class AccountCommentCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);
        setGraphic(null);

        if (empty || account == null) {
            setText("");
        } else {
            setText(account.comment());
        }
    }
}
