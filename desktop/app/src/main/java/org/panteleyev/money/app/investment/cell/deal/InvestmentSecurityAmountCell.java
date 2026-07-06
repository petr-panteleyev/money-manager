// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment.cell.deal;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.dto.InvestmentOperationType;
import org.panteleyev.money.model.InvestmentDeal;

public class InvestmentSecurityAmountCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            var amount = investmentDeal.securityAmount();
            if (investmentDeal.operationType() == InvestmentOperationType.SELL) {
                amount = -amount;
            }

            setText(Integer.toString(amount));
        }
    }
}
