package org.panteleyev.money.app.cells;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TableCell;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateCell<T> extends TableCell<T, LocalDate> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        setText(empty || date == null || LocalDate.EPOCH.equals(date)? "" : FORMATTER.format(date));
    }
}
