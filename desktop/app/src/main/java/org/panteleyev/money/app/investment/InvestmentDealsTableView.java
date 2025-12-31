// Copyright © 2024-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.fx.PredicateProperty;
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
import java.util.function.Predicate;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableObjectColumn;
import static org.panteleyev.fx.factories.TableFactory.tableStringColumn;
import static org.panteleyev.money.app.Comparators.investmentDealByDealDate;
import static org.panteleyev.money.app.GlobalContext.cache;

final class InvestmentDealsTableView extends TableView<InvestmentDeal> {
    private final FilteredList<InvestmentDeal> filteredList = new FilteredList<>(cache().getInvestmentDeals());
    private final PredicateProperty<InvestmentDeal> dealPredicateProperty = new PredicateProperty<>(_ -> false);

    public InvestmentDealsTableView(FilteredList<InvestmentDeal> list) {
        var w = widthProperty().subtract(20);

        TableColumn<InvestmentDeal, InvestmentDeal> dealDateColumn = apply(tableObjectColumn("Дата заключ."), c -> {
            c.setCellFactory(_ -> new InvestmentDealDateCell());
            c.comparator(investmentDealByDealDate());
            c.widthBinding(w.multiply(0.05));
        });

        getColumns().setAll(List.of(
                apply(tableObjectColumn("Счёт"), c -> {
                    c.setCellFactory(_ -> new InvestmentAccountCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableStringColumn("Сделка"), c -> {
                    c.valueConverter(InvestmentDeal::dealNumber);
                    c.widthBinding(w.multiply(0.05));
                }),
                dealDateColumn,
                apply(tableObjectColumn("Дата расчёта"), c -> {
                    c.setCellFactory(_ -> new InvestmentAccountingDateCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Инструмент"), c -> {
                    c.setCellFactory(_ -> new InvestmentSecurityCell());
                    c.widthBinding(w.multiply(0.08));
                }),
                apply(tableObjectColumn("Название"), c -> {
                    c.setCellFactory(_ -> new InvestmentSecurityNameCell());
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableObjectColumn("Тип"), c -> {
                    c.setCellFactory(_ -> new InvestmentSecurityTypeCell());
                    c.widthBinding(w.multiply(0.15));
                }),
                apply(tableObjectColumn("Кол-во"), c -> {
                    c.setCellFactory(_ -> new InvestmentSecurityAmountCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Цена"), c -> {
                    c.setCellFactory(_ -> new InvestmentPriceCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("НКД"), c -> {
                    c.setCellFactory(_ -> new InvestmentAciCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Объём сделки"), c -> {
                    c.setCellFactory(_ -> new InvestmentDealVolumeCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Комм. биржи"), c -> {
                    c.setCellFactory(_ -> new InvestmentExchangeFeeCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Комм. брокера"), c -> {
                    c.setCellFactory(_ -> new InvestmentBrokerFeeCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(tableObjectColumn("Общая сумма"), c -> {
                    c.setCellFactory(_ -> new InvestmentAmountCell());
                    c.widthBinding(w.multiply(0.05));
                })
        ));

        filteredList.predicateProperty().bind(dealPredicateProperty);

        var sortedList = filteredList.sorted();
        sortedList.comparatorProperty().bind(comparatorProperty());
        setItems(sortedList);

        getSortOrder().addAll(List.of(
                dealDateColumn
        ));
        dealDateColumn.setSortType(TableColumn.SortType.DESCENDING);

        sort();
    }

    public void setTransactionFilter(Predicate<InvestmentDeal> filter) {
        dealPredicateProperty.set(filter);
    }
}
