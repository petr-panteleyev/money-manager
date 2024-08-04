/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.deal;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentOperationType;

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
