// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment.cell.deal;

import javafx.scene.control.TableCell;
import org.panteleyev.money.model.ExchangeSecurity;
import org.panteleyev.money.model.InvestmentDeal;

import static org.panteleyev.money.app.GlobalContext.cache;

public class InvestmentSecurityCell extends TableCell<InvestmentDeal, InvestmentDeal> {
    @Override
    public void updateItem(InvestmentDeal investmentDeal, boolean empty) {
        super.updateItem(investmentDeal, empty);

        if (empty || investmentDeal == null) {
            setText("");
        } else {
            setText(cache().getExchangeSecurity(investmentDeal.securityUuid())
                    .map(ExchangeSecurity::secId)
                    .orElse(""));
        }
    }
}
