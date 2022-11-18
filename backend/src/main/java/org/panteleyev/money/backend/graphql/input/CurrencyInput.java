/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.input;

import java.math.BigDecimal;

public record CurrencyInput(
        String symbol,
        String description,
        String formatSymbol,
        int formatSymbolPosition,
        boolean showFormatSymbol,
        boolean def,
        BigDecimal rate,
        int direction,
        boolean useThousandSeparator
) {
}
