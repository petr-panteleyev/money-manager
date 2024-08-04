/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum ReportType {
    ACCOUNTS,
    TRANSACTIONS,
    INCOMES_AND_EXPENSES,
    STATEMENT;

    public String generateReportName() {
        return name().toLowerCase() + "-" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
