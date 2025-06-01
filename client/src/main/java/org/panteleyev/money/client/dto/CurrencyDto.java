/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CurrencyDto(
        UUID uuid,
        String symbol,
        String description,
        String formatSymbol,
        int formatSymbolPosition,
        boolean showFormatSymbol,
        boolean def,
        BigDecimal rate,
        int direction,
        boolean useThousandSeparator,
        long created,
        long modified
) implements MoneyDto {
}
