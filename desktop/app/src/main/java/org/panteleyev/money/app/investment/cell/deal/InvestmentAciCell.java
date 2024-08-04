/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.deal;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.math.RoundingMode;

public class InvestmentAciCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        setAlignment(Pos.CENTER_RIGHT);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            setText(investmentDeal.aci().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }
}
