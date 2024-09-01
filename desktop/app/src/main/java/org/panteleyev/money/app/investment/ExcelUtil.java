/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;

final class ExcelUtil {
    static String getCellValueAsString(Cell cell) {
        var formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private ExcelUtil() {
    }
}
