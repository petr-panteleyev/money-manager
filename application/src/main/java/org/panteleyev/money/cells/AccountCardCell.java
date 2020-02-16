package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import org.panteleyev.money.model.Account;
import static org.panteleyev.money.Images.getCardTypeIcon;

public class AccountCardCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        setText("");
        setGraphic(null);

        if (empty || account == null || account.getCardNumber().isBlank()) {
            return;
        }

        setText(account.getCardNumber());
        setGraphic(new ImageView(getCardTypeIcon(account.getCardType())));
    }
}
