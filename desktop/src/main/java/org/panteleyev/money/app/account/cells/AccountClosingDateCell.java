/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.panteleyev.money.app.Constants.FULL_DATE_FORMAT;
import static org.panteleyev.money.app.Styles.EXPIRED;

public class AccountClosingDateCell extends TableCell<Account, Account> {
    private final int delta;

    public AccountClosingDateCell(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be >= 0");
        }

        this.delta = delta;
    }

    @Override
    public void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        getStyleClass().remove(EXPIRED);

        if (empty || account == null || account.closingDate() == null) {
            setText("");
        } else {
            if (LocalDate.now().until(account.closingDate(), ChronoUnit.DAYS) < delta) {
                getStyleClass().add(EXPIRED);
            }
            setText(LocalDate.EPOCH.equals(account.closingDate()) ?
                    "" : FULL_DATE_FORMAT.format(account.closingDate()));
        }
    }
}
