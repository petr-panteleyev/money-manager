/*
 Copyright © 2021-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account.cells;

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
