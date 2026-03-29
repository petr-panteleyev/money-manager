// Copyright © 2024-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.app.investment.cell.deal.InvestmentAccountCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentAccountingDateCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentAciCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentAmountCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentBrokerFeeCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentDealDateCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentDealVolumeCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentExchangeFeeCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentPriceCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentSecurityAmountCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentSecurityCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentSecurityNameCell;
import org.panteleyev.money.app.investment.cell.deal.InvestmentSecurityTypeCell;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.util.List;

import static org.panteleyev.money.app.Comparators.investmentDealByDealDate;

final class InvestmentDealsTableView extends TableView<InvestmentDeal> {
    public InvestmentDealsTableView(FilteredList<InvestmentDeal> list) {
        var w = widthProperty().subtract(20);

        var accountColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Счёт");
        accountColumn.setCellFactory(_ -> new InvestmentAccountCell());
        accountColumn.widthBinding(w.multiply(0.05));

        var dealColumn = TableFactory.<InvestmentDeal>tableStringColumn("Сделка");
        dealColumn.valueConverter(InvestmentDeal::dealNumber);
        dealColumn.widthBinding(w.multiply(0.05));

        var dealDateColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Дата заключ.");
        dealDateColumn.setCellFactory(_ -> new InvestmentDealDateCell());
        dealDateColumn.comparator(investmentDealByDealDate());
        dealDateColumn.widthBinding(w.multiply(0.05));

        var accountingDateColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Дата расчёта");
        accountingDateColumn.setCellFactory(_ -> new InvestmentAccountingDateCell());
        accountingDateColumn.widthBinding(w.multiply(0.05));

        var securityColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Инструмент");
        securityColumn.setCellFactory(_ -> new InvestmentSecurityCell());
        securityColumn.widthBinding(w.multiply(0.08));

        var nameColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Название");
        nameColumn.setCellFactory(_ -> new InvestmentSecurityNameCell());
        nameColumn.widthBinding(w.multiply(0.2));

        var typeColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Тип");
        typeColumn.setCellFactory(_ -> new InvestmentSecurityTypeCell());
        typeColumn.widthBinding(w.multiply(0.15));

        var amountColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Кол-во");
        amountColumn.setCellFactory(_ -> new InvestmentSecurityAmountCell());
        amountColumn.widthBinding(w.multiply(0.05));

        var priceColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Цена");
        priceColumn.setCellFactory(_ -> new InvestmentPriceCell());
        priceColumn.widthBinding(w.multiply(0.05));

        var aciColumn = TableFactory.<InvestmentDeal>tableObjectColumn("НКД");
        aciColumn.setCellFactory(_ -> new InvestmentAciCell());
        aciColumn.widthBinding(w.multiply(0.05));

        var volumeColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Объём сделки");
        volumeColumn.setCellFactory(_ -> new InvestmentDealVolumeCell());
        volumeColumn.widthBinding(w.multiply(0.05));

        var exchangeFeeColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Комм. биржи");
        exchangeFeeColumn.setCellFactory(_ -> new InvestmentExchangeFeeCell());
        exchangeFeeColumn.widthBinding(w.multiply(0.05));

        var brokerFeeColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Комм. брокера");
        brokerFeeColumn.setCellFactory(_ -> new InvestmentBrokerFeeCell());
        brokerFeeColumn.widthBinding(w.multiply(0.05));

        var totalColumn = TableFactory.<InvestmentDeal>tableObjectColumn("Общая сумма");
        totalColumn.setCellFactory(_ -> new InvestmentAmountCell());
        totalColumn.widthBinding(w.multiply(0.05));

        getColumns().setAll(List.of(
                accountColumn, dealColumn, dealDateColumn, accountingDateColumn,
                securityColumn, nameColumn, typeColumn, amountColumn,
                priceColumn, aciColumn, volumeColumn, exchangeFeeColumn,
                brokerFeeColumn,
                totalColumn));

        var sortedList = list.sorted();
        sortedList.comparatorProperty().bind(comparatorProperty());
        setItems(sortedList);

        getSortOrder().add(dealDateColumn);
        dealDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        sort();
    }
}
