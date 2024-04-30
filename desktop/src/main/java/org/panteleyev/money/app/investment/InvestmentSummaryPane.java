/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.exchange.Definitions.STOCK_BONDS;

public class InvestmentSummaryPane extends BorderPane {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<InvestmentDeal> dealsChangeListener =
            _ -> Platform.runLater(this::onDealsChanged);

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<ExchangeSecurity> securityChangeListener =
            _ -> Platform.runLater(this::onDealsChanged);

    private final InvestmentSummaryTableView tableView = new InvestmentSummaryTableView();

    public InvestmentSummaryPane() {
        setCenter(tableView);
        onDealsChanged();

        cache().getExchangeSecurities().addListener(new WeakListChangeListener<>(securityChangeListener));
        cache().getInvestmentDeals().addListener(new WeakListChangeListener<>(dealsChangeListener));
    }

    private List<InvestmentSummary> calculateSummary(List<InvestmentDeal> deals) {
        var summaries = new HashMap<UUID, InvestmentSummary>();

        for (var deal : deals) {
            summaries.compute(deal.securityUuid(), (_, summary) -> add(summary, deal));
        }

        var totalValueOfAll = BigDecimal.ZERO;
        var result = new ArrayList<InvestmentSummary>();

        var today = LocalDate.now();

        for (var summary : summaries.values()) {
            if (summary.securityAmount() == 0) {
                continue;
            }

            var security = cache().getExchangeSecurity(summary.securityUuid()).orElse(null);
            if (security == null) {
                continue;
            }
            if (security.matDate() != null && security.matDate().isBefore(today)) {
                continue;
            }

            var averagePrice = summary.totalPurchaseValue()
                    .divide(new BigDecimal(summary.totalPurchaseAmount()), RoundingMode.HALF_UP);
            var totalValue = calculateTotalValue(security, summary.securityAmount());

            totalValueOfAll = totalValueOfAll.add(totalValue);

            result.add(
                    new InvestmentSummary(
                            summary.securityUuid(),
                            averagePrice,
                            summary.securityAmount(),
                            totalValue,
                            summary.percentage(),
                            summary.totalExchangeFee(),
                            summary.totalBrokerFee(),
                            summary.totalPurchaseAmount(),
                            summary.totalPurchaseValue()
                    )
            );
        }

        var totalValueOfAllFinal = totalValueOfAll;
        return result.stream().map(s -> s.withPercentage(
                s.totalValue().divide(totalValueOfAllFinal, RoundingMode.HALF_EVEN)
                        .multiply(ONE_HUNDRED)
        )).toList();
    }

    private static BigDecimal calculateTotalValue(ExchangeSecurity security, int amount) {
        var currentValue = security.group().equals(STOCK_BONDS) ?
                security.faceValue().multiply(security.marketValue()).divide(ONE_HUNDRED, RoundingMode.HALF_UP) :
                security.marketValue();

        return currentValue.multiply(BigDecimal.valueOf(amount));
    }

    private static InvestmentSummary add(InvestmentSummary summary, InvestmentDeal deal) {
        var amountInc = deal.operationType() == InvestmentOperationType.PURCHASE ?
                deal.securityAmount() : -deal.securityAmount();
        var totalPurchaseAmountInc = deal.operationType() == InvestmentOperationType.PURCHASE ?
                deal.securityAmount() : 0;
        var totalPurchaseValueInc = deal.operationType() == InvestmentOperationType.PURCHASE ?
                deal.amount() : BigDecimal.ZERO;

        if (summary == null) {
            return new InvestmentSummary(
                    deal.securityUuid(),
                    BigDecimal.ZERO,
                    amountInc,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    deal.exchangeFee(),
                    deal.brokerFee(),
                    totalPurchaseAmountInc,
                    totalPurchaseValueInc
            );
        } else {
            return new InvestmentSummary(
                    deal.securityUuid(),
                    BigDecimal.ZERO,
                    summary.securityAmount() + amountInc,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    summary.totalExchangeFee().add(deal.exchangeFee()),
                    summary.totalBrokerFee().add(deal.brokerFee()),
                    summary.totalPurchaseAmount() + totalPurchaseAmountInc,
                    summary.totalPurchaseValue().add(totalPurchaseValueInc)
            );
        }
    }

    private void onDealsChanged() {
        tableView.setList(
                calculateSummary(cache().getInvestmentDeals())
        );
    }
}
