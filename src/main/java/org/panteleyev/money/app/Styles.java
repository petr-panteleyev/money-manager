/*
 Copyright (C) 2017, 2019, 2020, 2021, 2022 Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
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
