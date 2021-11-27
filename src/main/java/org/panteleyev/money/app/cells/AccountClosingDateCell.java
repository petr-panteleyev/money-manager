/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
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
