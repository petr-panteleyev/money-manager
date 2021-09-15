/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Currency;
import static org.panteleyev.money.app.GlobalContext.cache;

public class AccountBalanceCell extends TableCell<Account, Account> {
    private final boolean total;

    public AccountBalanceCell(boolean total) {
        this.total = total;
    }

    @Override
    public void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (empty || account == null) {
            setText("");
        } else {
            var sum = total ? account.getBalance() : account.totalWaiting();

            setText(cache().getCurrency(account.currencyUuid())
                .map(curr -> curr.formatValue(sum))
                .orElse(Currency.defaultFormatValue(sum)));

            getStyleClass().add(
                sum.signum() < 0 ? Styles.DEBIT : Styles.CREDIT
            );
        }
    }
}
