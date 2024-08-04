/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.fx.TableColumnBuilder;
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

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.Comparators.investmentDealByDealDate;

final class InvestmentDealsTableView extends TableView<InvestmentDeal> {
    private static final int DEAL_DATE_COLUMN_INDEX = 3;

    public InvestmentDealsTableView(SortedList<InvestmentDeal> list) {
        super(list);

        var w = widthProperty().subtract(20);

        TableColumn<InvestmentDeal, InvestmentDeal> dealDateColumn = tableObjectColumn("Дата заключ.", b ->
                b.withCellFactory(_ -> new InvestmentDealDateCell())
                        .withComparator(investmentDealByDealDate())
                        .withWidthBinding(w.multiply(0.05)));

        getColumns().setAll(List.of(
                tableObjectColumn("Счёт", b ->
                        b.withCellFactory(_ -> new InvestmentAccountCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableColumn("Сделка", (TableColumnBuilder<InvestmentDeal, String> b) ->
                        b.withPropertyCallback(InvestmentDeal::dealNumber)
                                .withWidthBinding(w.multiply(0.05))),
                dealDateColumn,
                tableObjectColumn("Дата расчёта", b ->
                        b.withCellFactory(_ -> new InvestmentAccountingDateCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Инструмент", b ->
                        b.withCellFactory(_ -> new InvestmentSecurityCell())
                                .withWidthBinding(w.multiply(0.08))),
                tableObjectColumn("Название", b ->
                        b.withCellFactory(_ -> new InvestmentSecurityNameCell())
                                .withWidthBinding(w.multiply(0.2))),
                tableObjectColumn("Тип", b ->
                        b.withCellFactory(_ -> new InvestmentSecurityTypeCell())
                                .withWidthBinding(w.multiply(0.15))),
                tableObjectColumn("Кол-во", b ->
                        b.withCellFactory(_ -> new InvestmentSecurityAmountCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Цена", b ->
                        b.withCellFactory(_ -> new InvestmentPriceCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("НКД", b ->
                        b.withCellFactory(_ -> new InvestmentAciCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Объём сделки", b ->
                        b.withCellFactory(_ -> new InvestmentDealVolumeCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Комм. биржи", b ->
                        b.withCellFactory(_ -> new InvestmentExchangeFeeCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Комм. брокера", b ->
                        b.withCellFactory(_ -> new InvestmentBrokerFeeCell())
                                .withWidthBinding(w.multiply(0.05))),
                tableObjectColumn("Общая сумма", b ->
                        b.withCellFactory(_ -> new InvestmentAmountCell())
                                .withWidthBinding(w.multiply(0.05)))
        ));

        list.comparatorProperty().bind(comparatorProperty());

        getSortOrder().addAll(List.of(
                dealDateColumn
        ));
        dealDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        sort();
    }
}
