/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account.cells;

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
