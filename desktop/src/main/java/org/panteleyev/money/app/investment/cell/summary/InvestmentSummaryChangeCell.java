/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.summary;

import javafx.geometry.Pos;
import javafx.scene.control.TreeTableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.investment.InvestmentSummaryTreeData;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.math.RoundingMode;

public class InvestmentSummaryChangeCell extends TreeTableCell<InvestmentSummaryTreeData, InvestmentSummaryTreeData> {
    @Override
    protected void updateItem(InvestmentSummaryTreeData summary, boolean empty) {
        super.updateItem(summary, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (summary == null || empty) {
            setText("");
        } else {
            getStyleClass().add(
                    summary.change().signum() == -1 ? Styles.DEBIT : Styles.CREDIT
            );

            setText(summary.change().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
