package org.panteleyev.money.app.cells;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Account;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import static org.panteleyev.money.app.Styles.RED_TEXT;

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

        setText("");
        getStyleClass().remove(RED_TEXT);

        if (empty || account == null || account.closingDate() == null) {
            return;
        }

        if (LocalDate.now().until(account.closingDate(), ChronoUnit.DAYS) < delta) {
            getStyleClass().add(RED_TEXT);
        }
        var formatter = account.cardNumber().isBlank() ? FORMATTER : CARD_FORMATTER;
        setText(LocalDate.EPOCH.equals(account.closingDate()) ? "" : formatter.format(account.closingDate()));
    }
}
