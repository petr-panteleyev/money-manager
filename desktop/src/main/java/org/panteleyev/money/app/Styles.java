/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

public interface Styles {
    double BIG_SPACING = 5.0;
    double SMALL_SPACING = 2.0;
    double DOUBLE_SPACING = BIG_SPACING * 2;

    // Amounts
    String DEBIT = "amount-debit";
    String CREDIT = "amount-credit";
    String TRANSFER = "amount-transfer";

    // Account expiration date
    String EXPIRED = "expired";

    String GROUP_CELL = "groupCell";

    String BOLD_TEXT = "boldText";

    String GRID_PANE = "gridPane";

    // About Dialog
    String STYLE_ABOUT_LABEL = "aboutLabel";

    // Transaction Editor
    String RATE_LABEL = "rateLabel";
    String SUB_LABEL = "subLabel";

    // Statement
    String STATEMENT_NOT_FOUND = "statementMissing";
    String STATEMENT_ALL_CHECKED = "statementChecked";
    String STATEMENT_NOT_CHECKED = "statementUnchecked";
}
