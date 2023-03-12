/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.PeriodicPayment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.panteleyev.money.app.Styles.EXPIRED;

public class PeriodicPaymentNextDateCell extends TableCell<PeriodicPayment, PeriodicPayment> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final int delta;

    public PeriodicPaymentNextDateCell(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta must be >= 0");
        }

        this.delta = delta;
    }

    @Override
    public void updateItem(PeriodicPayment periodicPayment, boolean empty) {
        super.updateItem(periodicPayment, empty);

        getStyleClass().remove(EXPIRED);

        if (empty || periodicPayment == null) {
            setText("");
        } else {
            var nextDate = periodicPayment.calculateNextDate();
            if (LocalDate.now().until(nextDate, ChronoUnit.DAYS) < delta) {
                getStyleClass().add(EXPIRED);
            }
            setText(LocalDate.EPOCH.equals(nextDate) ? "" : FORMATTER.format(nextDate));
        }
    }
}
