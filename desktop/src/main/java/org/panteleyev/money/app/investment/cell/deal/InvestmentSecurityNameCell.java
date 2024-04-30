/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment.cell.deal;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.investment.InvestmentDeal;

import static org.panteleyev.money.app.GlobalContext.cache;

public class InvestmentSecurityNameCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            setText(cache().getExchangeSecurity(investmentDeal.securityUuid())
                    .map(ExchangeSecurity::name)
                    .orElse(""));
        }
    }
}
