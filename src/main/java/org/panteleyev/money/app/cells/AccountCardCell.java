/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import org.panteleyev.money.model.Account;
import static org.panteleyev.money.app.Images.getCardTypeIcon;

public class AccountCardCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        setText("");
        setGraphic(null);

        if (empty || account == null || account.cardNumber().isBlank()) {
            return;
        }

        setText(account.cardNumber());
        setGraphic(new ImageView(getCardTypeIcon(account.cardType())));
    }
}
