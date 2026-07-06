// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment.cell.deal;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.InvestmentDeal;

import java.time.format.DateTimeFormatter;

public class InvestmentDealDateCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            setText(DATE_TIME_FORMATTER.format(investmentDeal.dealDate()));
        }
    }
}
