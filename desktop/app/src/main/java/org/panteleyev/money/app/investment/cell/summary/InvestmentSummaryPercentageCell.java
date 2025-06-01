/*
 Copyright © 2024-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.summary;

import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import org.panteleyev.money.app.investment.InvestmentSummaryTreeData;

import java.math.RoundingMode;

public class InvestmentSummaryPercentageCell extends TreeTableCell<InvestmentSummaryTreeData, InvestmentSummaryTreeData> {
    @Override
    protected void updateItem(InvestmentSummaryTreeData summary, boolean empty) {
        super.updateItem(summary, empty);

        if (summary == null || empty) {
            setText("");
        } else {
            setAlignment(Pos.CENTER_RIGHT);
            setText(summary.percentage().setScale(2, RoundingMode.HALF_EVEN) + "%");
        }
    }
}
