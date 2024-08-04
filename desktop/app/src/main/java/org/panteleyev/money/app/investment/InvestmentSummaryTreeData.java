/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import java.math.BigDecimal;

public record InvestmentSummaryTreeData (
    String groupName,
    String securityId,
    String instrumentName,
    BigDecimal securityAmount,
    BigDecimal averagePrice,
    BigDecimal totalValue,
    BigDecimal change,
    BigDecimal percentage,
    BigDecimal totalExchangeFee,
    BigDecimal totalBrokerFee
) {
}

