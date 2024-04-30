/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.deal;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.math.RoundingMode;

public class InvestmentAmountCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        setAlignment(Pos.CENTER_RIGHT);
        getStyleClass().removeAll(Styles.CREDIT, Styles.DEBIT);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            var amount = investmentDeal.amount().setScale(2, RoundingMode.HALF_UP);
            getStyleClass().add(
                    investmentDeal.operationType() == InvestmentOperationType.PURCHASE ? Styles.DEBIT : Styles.CREDIT
            );

            setText(amount.toString());
        }
    }
}
