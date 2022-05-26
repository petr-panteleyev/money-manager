/*
 Copyright (C) 2017, 2018, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.panteleyev.money.app.Styles.EXPIRED;

public class AccountClosingDateCell extends TableCell<Account, Account> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter CARD_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

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
            var formatter = account.cardNumber().isBlank() ? FORMATTER : CARD_FORMATTER;
            setText(LocalDate.EPOCH.equals(account.closingDate()) ? "" : formatter.format(account.closingDate()));
        }
    }
}
