// Copyright © 2024-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryAmountCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryAveragePriceCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryBrokerFeeCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryChangeCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryExchangeFeeCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryInstrumentCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryInstrumentNameCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryPercentageCell;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryRow;
import org.panteleyev.money.app.investment.cell.summary.InvestmentSummaryTotalValueCell;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TreeTableFactory.treeTableObjectColumn;
import static org.panteleyev.money.app.Comparators.investmentSummaryTreeDataByPercentage;
import static org.panteleyev.money.app.GlobalContext.cache;

public class InvestmentSummaryTableView extends TreeTableView<InvestmentSummaryTreeData> {
    private final TreeTableColumn<InvestmentSummaryTreeData, InvestmentSummaryTreeData> percentageColumn;

    public InvestmentSummaryTableView() {
        setShowRoot(false);

        var w = widthProperty().subtract(20);

        percentageColumn = apply(treeTableObjectColumn("Доля"), c -> {
            c.setCellFactory(_ -> new InvestmentSummaryPercentageCell());
            c.comparator(investmentSummaryTreeDataByPercentage());
            c.widthBinding(w.multiply(0.05));
            c.setSortType(TreeTableColumn.SortType.DESCENDING);
        });

        getColumns().setAll(List.of(
                apply(treeTableObjectColumn("Инструмент"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryInstrumentCell());
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(treeTableObjectColumn("Название"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryInstrumentNameCell());
                    c.widthBinding(w.multiply(0.2));
                }),
                percentageColumn,
                apply(treeTableObjectColumn("Кол-во"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryAmountCell());
                    c.widthBinding(w.multiply(0.05));
                }),
                apply(treeTableObjectColumn("Ср. цена"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryAveragePriceCell());
                    c.widthBinding(w.multiply(0.1));
                }),
                apply(treeTableObjectColumn("Тек. стоимость"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryTotalValueCell());
                    c.widthBinding(w.multiply(0.1));
                }),
                apply(treeTableObjectColumn("Изм. стоимости"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryChangeCell());
                    c.widthBinding(w.multiply(0.1));
                }),
                apply(treeTableObjectColumn("Комм. биржи"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryExchangeFeeCell());
                    c.widthBinding(w.multiply(0.1));
                }),
                apply(treeTableObjectColumn("Комм. брокера"), c -> {
                    c.setCellFactory(_ -> new InvestmentSummaryBrokerFeeCell());
                    c.widthBinding(w.multiply(0.1));
                })
        ));

        setRowFactory(_ -> new InvestmentSummaryRow());
    }

    public void setList(List<InvestmentSummary> list) {
        setupTree(list);
    }

    private void setupTree(List<InvestmentSummary> list) {
        var groupByGroupName = list.stream().collect(Collectors.groupingBy(s ->
                cache().getExchangeSecurity(s.securityUuid()).map(ExchangeSecurity::groupName).orElse("")
        ));

        var root = new TreeItem<InvestmentSummaryTreeData>();

        for (var entry : groupByGroupName.entrySet()) {
            if (entry.getKey().isBlank()) {
                continue;
            }

            var groupTotalAmount = BigDecimal.ZERO;
            var groupPercentage = BigDecimal.ZERO;
            var groupTotalValue = BigDecimal.ZERO;
            var groupChange = BigDecimal.ZERO;
            var groupTotalExchangeFee = BigDecimal.ZERO;
            var groupTotalBrokerFee = BigDecimal.ZERO;

            ObservableList<TreeItem<InvestmentSummaryTreeData>> groupChildren = FXCollections.observableArrayList();

            for (var summary : entry.getValue()) {
                var security = cache().getExchangeSecurity(summary.securityUuid()).orElse(null);
                if (security == null) {
                    continue;
                }

                var data = new InvestmentSummaryTreeData(
                        "",
                        security.secId(),
                        security.name(),
                        summary.securityAmount(),
                        summary.averagePrice(),
                        summary.totalValue(),
                        summary.totalValue().subtract(summary.averagePrice()
                                .multiply(summary.securityAmount())),
                        summary.percentage(),
                        summary.totalExchangeFee(),
                        summary.totalBrokerFee()
                );

                groupTotalAmount = groupTotalAmount.add(data.securityAmount());
                groupPercentage = groupPercentage.add(data.percentage());
                groupTotalValue = groupTotalValue.add(data.totalValue());
                groupChange = groupChange.add(data.change());
                groupTotalExchangeFee = groupTotalExchangeFee.add(data.totalExchangeFee());
                groupTotalBrokerFee = groupTotalBrokerFee.add(data.totalBrokerFee());

                groupChildren.add(new TreeItem<>(data));
            }

            var groupTreeItem = new TreeItem<>(
                    new InvestmentSummaryTreeData(
                            entry.getKey(),
                            "", "", groupTotalAmount,
                            BigDecimal.ZERO,
                            groupTotalValue,
                            groupChange,
                            groupPercentage,
                            groupTotalExchangeFee,
                            groupTotalBrokerFee
                    )
            );
            groupTreeItem.getChildren().setAll(groupChildren);
            groupTreeItem.setExpanded(true);

            root.getChildren().add(groupTreeItem);
        }

        root.setExpanded(true);
        setRoot(root);

        getSortOrder().add(percentageColumn);
        Platform.runLater(this::sort);
    }
}
