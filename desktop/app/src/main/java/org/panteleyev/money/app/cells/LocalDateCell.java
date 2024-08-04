/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.cells;

import javafx.scene.control.TableCell;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateCell<T> extends TableCell<T, LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        setText(empty || date == null || LocalDate.EPOCH.equals(date) ? "" : FORMATTER.format(date));
    }
}
