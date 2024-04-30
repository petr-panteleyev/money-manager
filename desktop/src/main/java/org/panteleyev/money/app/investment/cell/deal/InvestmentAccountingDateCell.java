/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.deal;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.time.format.DateTimeFormatter;

public class InvestmentAccountingDateCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            setText(DATE_TIME_FORMATTER.format(investmentDeal.accountingDate()));
        }
    }
}
