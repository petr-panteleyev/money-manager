// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.investment;

import org.panteleyev.money.dto.InvestmentOperationType;

import java.util.Map;

final class ParserUtil {
    private static final Map<String, InvestmentOperationType> INVESTMENT_OPERATION_TYPE_MAP = Map.of(
            "Покупка", InvestmentOperationType.PURCHASE,
            "Продажа", InvestmentOperationType.SELL
    );

    static InvestmentOperationType parseInvestmentOperationType(String title) {
        if (title == null) return InvestmentOperationType.UNKNOWN;
        var type = INVESTMENT_OPERATION_TYPE_MAP.get(title);
        return type != null ? type : InvestmentOperationType.UNKNOWN;
    }

    private ParserUtil(){}
}
