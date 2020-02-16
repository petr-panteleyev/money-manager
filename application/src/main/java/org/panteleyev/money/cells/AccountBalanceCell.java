package org.panteleyev.money.cells;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import java.util.function.Predicate;
import static org.panteleyev.money.Styles.RED_TEXT;
import static org.panteleyev.money.persistence.DataCache.cache;

public class AccountBalanceCell extends TableCell<Account, Account> {
    private final boolean total;
    private final Predicate<Transaction> filter;

    public AccountBalanceCell(boolean total, Predicate<Transaction> filter) {
        this.total = total;
        this.filter = filter.and(t -> t.getParentUuid().isEmpty());
    }

    @Override
    public void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || account == null) {
            setText("");
        } else {
            var sum = cache().calculateBalance(account, total, filter);

            // TODO: use flatMap
            setText(cache().getCurrency(account.getCurrencyUuid().orElse(null))
                .map(curr -> curr.formatValue(sum))
                .orElse(Currency.defaultFormatValue(sum)));

            getStyleClass().remove(RED_TEXT);
            if (sum.signum() < 0) {
                getStyleClass().add(RED_TEXT);
            }
        }
    }
}
