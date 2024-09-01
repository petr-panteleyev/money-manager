/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.geometry.Insets;

public final class Styles {
    public static final double BIG_SPACING = 5.0;
    public static final double SMALL_SPACING = 2.0;
    public static final double DOUBLE_SPACING = BIG_SPACING * 2;

    public static final Insets BIG_INSETS = new Insets(BIG_SPACING);
    public static final Insets DOUBLE_INSETS = new Insets(DOUBLE_SPACING);

    // Amounts
    public static final String DEBIT = "amount-debit";
    public static final String CREDIT = "amount-credit";
    public static final String TRANSFER = "amount-transfer";

    // Account expiration date
    public static final String EXPIRED = "expired";

    public static final String GROUP_CELL = "groupCell";

    public static final String BOLD_TEXT = "boldText";

    public static final String GRID_PANE = "gridPane";

    // About Dialog
    public static final String STYLE_ABOUT_LABEL = "aboutLabel";

    // Transaction Editor
    public static final String RATE_LABEL = "rateLabel";
    public static final String SUB_LABEL = "subLabel";

    // Statement
    public static final String STATEMENT_NOT_FOUND = "statementMissing";
    public static final String STATEMENT_ALL_CHECKED = "statementChecked";
    public static final String STATEMENT_NOT_CHECKED = "statementUnchecked";

    private Styles() {
    }
}
