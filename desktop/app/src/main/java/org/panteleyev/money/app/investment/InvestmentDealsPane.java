/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import javafx.collections.ObservableList;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.investment.InvestmentDeal;

import static org.panteleyev.money.app.GlobalContext.cache;

public class InvestmentDealsPane extends BorderPane {
    private final ObservableList<InvestmentDeal> filteredInvestmentDeals = cache().getInvestmentDeals();
    private final InvestmentDealsTableView tableView = new InvestmentDealsTableView(filteredInvestmentDeals.sorted());

    public InvestmentDealsPane() {
        setCenter(tableView);
    }
}
