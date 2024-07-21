/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import java.math.BigDecimal;
import java.util.UUID;

public record InvestmentSummary(
        UUID securityUuid,
        BigDecimal averagePrice,
        BigDecimal securityAmount,
        BigDecimal totalValue,
        BigDecimal percentage,
        BigDecimal totalExchangeFee,
        BigDecimal totalBrokerFee,
        // Доп. данные для рассчета
        BigDecimal totalPurchaseAmount,
        BigDecimal totalPurchaseValue
) {
    public InvestmentSummary withPercentage(BigDecimal percentage) {
        return new InvestmentSummary(
                securityUuid(),
                averagePrice(),
                securityAmount(),
                totalValue(),
                percentage,
                totalExchangeFee(),
                totalBrokerFee(),
                totalPurchaseAmount(),
                totalPurchaseValue()
        );
    }
}
