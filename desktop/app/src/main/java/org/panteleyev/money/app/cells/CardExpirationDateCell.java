/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.Card;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.panteleyev.money.app.Styles.EXPIRED;

public class CardExpirationDateCell extends TableCell<Card, Card> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    private final int delta;

    public CardExpirationDateCell(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be >= 0");
        }

        this.delta = delta;
    }

    @Override
    public void updateItem(Card card, boolean empty) {
        super.updateItem(card, empty);

        getStyleClass().remove(EXPIRED);

        if (empty || card == null || card.expiration() == null) {
            setText("");
        } else {
            if (LocalDate.now().until(card.expiration(), ChronoUnit.DAYS) < delta) {
                getStyleClass().add(EXPIRED);
            }
            setText(FORMATTER.format(card.expiration()));
        }
    }
}
