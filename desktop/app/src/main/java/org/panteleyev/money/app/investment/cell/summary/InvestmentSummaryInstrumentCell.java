/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.summary;

import javafx.scene.control.TreeTableCell;
import org.panteleyev.money.app.investment.InvestmentSummaryTreeData;

public class InvestmentSummaryInstrumentCell extends TreeTableCell<InvestmentSummaryTreeData, InvestmentSummaryTreeData> {
    @Override
    protected void updateItem(InvestmentSummaryTreeData summary, boolean empty) {
        super.updateItem(summary, empty);

        if (summary == null || empty) {
            setText("");
        } else {
            if (summary.groupName().isBlank()) {
                setText(summary.securityId());
            } else {
                setText(summary.groupName());
            }
        }
    }
}
