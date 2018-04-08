/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.cells;

import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import org.panteleyev.money.AccountTreeItem;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.Transaction;
import java.util.function.Predicate;
import static org.panteleyev.money.Styles.RED_TEXT;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

public class AccountBalanceCell extends TreeTableCell<AccountTreeItem, Account> {
    private final boolean total;
    private final Predicate<Transaction> filter;

    public AccountBalanceCell(Predicate<Transaction> filter) {
        this(false, filter);
    }

    public AccountBalanceCell(boolean total, Predicate<Transaction> filter) {
        this.total = total;
        this.filter = filter;
    }

    @Override
    public void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || account == null) {
            setText("");
        } else {
            var sum = account.calculateBalance(total, filter);

            setText(getDao().getCurrency(account.getCurrencyId())
                    .map(curr -> curr.formatValue(sum))
                    .orElse(Currency.defaultFormatValue(sum)));

            getStyleClass().remove(RED_TEXT);
            if (sum.signum() < 0) {
                getStyleClass().add(RED_TEXT);
            }
        }
    }
}
