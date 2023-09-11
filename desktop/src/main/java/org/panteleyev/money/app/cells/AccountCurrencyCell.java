/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import static org.panteleyev.money.app.GlobalContext.cache;

public class AccountCurrencyCell extends TableCell<Account, Account> {
    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);
        setGraphic(null);

        if (empty || account == null) {
            setText("");
        } else {
            String text = "";
            if (account.currencyUuid() != null) {
                text = cache().getCurrency(account.currencyUuid())
                        .map(Currency::symbol)
                        .orElse("");
            } else if (account.securityUuid() != null) {
                text = cache().getExchangeSecurity(account.securityUuid())
                        .map(ExchangeSecurity::shortName)
                        .orElse("");
            }

            setText(text);
        }
    }
}
