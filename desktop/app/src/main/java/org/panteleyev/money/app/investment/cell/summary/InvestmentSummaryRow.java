/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.summary;

import javafx.scene.control.TreeTableRow;
import org.panteleyev.money.app.investment.InvestmentSummaryTreeData;

import static org.panteleyev.money.app.Styles.GROUP_CELL;

public class InvestmentSummaryRow extends TreeTableRow<InvestmentSummaryTreeData> {
    @Override
    public void updateItem(InvestmentSummaryTreeData item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().removeAll(GROUP_CELL);

        if (item != null && !empty && !item.groupName().isBlank()) {
            getStyleClass().add(GROUP_CELL);
        }
    }
}
