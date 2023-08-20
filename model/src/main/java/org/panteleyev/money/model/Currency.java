/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record Currency(
        UUID uuid,
        CurrencyType type,
        String symbol,
        String description,
        String formatSymbol,
        int formatSymbolPosition,
        boolean showFormatSymbol,
        boolean def,
        BigDecimal rate,
        int direction,
        boolean useThousandSeparator,
        String isin,
        String registry,
        long created,
        long modified
) implements MoneyRecord {

    public Currency {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalStateException("Currency symbol cannot be blank");
        }

        description = MoneyRecord.normalize(description);
        formatSymbol = MoneyRecord.normalize(formatSymbol);
        rate = MoneyRecord.normalize(rate, BigDecimal.ONE);

        long now = System.currentTimeMillis();
        if (created == 0) {
            created = now;
        }
        if (modified == 0) {
            modified = now;
        }
    }

    public String formatValue(BigDecimal value) {
        return switch (type()) {
            case CURRENCY -> {
                var sumString = value.abs().setScale(2, RoundingMode.HALF_UP).toString();
                var signString = value.signum() < 0 ? "-" : "";

                yield formatSymbolPosition == 0 ?
                        signString + formatSymbol + sumString :
                        signString + sumString + formatSymbol;
            }
            case SECURITY -> {
                var total = value.multiply(rate());
                yield total.setScale(2, RoundingMode.HALF_UP).toString();
            }
        };
    }

    public static String defaultFormatValue(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static final class Builder {
        private CurrencyType type = CurrencyType.CURRENCY;
        private String symbol = "";
        private String description = "";
        private String formatSymbol = "";
        private int formatSymbolPosition = 0;
        private boolean showFormatSymbol = false;
        private boolean def = false;
        private BigDecimal rate = BigDecimal.ONE;
        private int direction = 1;
        private boolean useThousandSeparator = false;
        private UUID uuid = null;
        private String isin = "";
        private String registry = "";
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public Builder(Currency c) {
            if (c == null) {
                return;
            }

            type = c.type();
            symbol = c.symbol();
            description = c.description();
            formatSymbol = c.formatSymbol();
            formatSymbolPosition = c.formatSymbolPosition();
            showFormatSymbol = c.showFormatSymbol();
            def = c.def();
            rate = c.rate();
            direction = c.direction();
            useThousandSeparator = c.useThousandSeparator();
            uuid = c.uuid();
            isin = c.isin();
            created = c.created();
            modified = c.modified();
        }

        public Currency build() {
            return new Currency(uuid, type, symbol, description, formatSymbol, formatSymbolPosition,
                    showFormatSymbol, def, rate, direction, useThousandSeparator,
                    isin, registry, created, modified);
        }

        public Builder type(CurrencyType type) {
            this.type = type;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder formatSymbol(String formatSymbol) {
            this.formatSymbol = formatSymbol;
            return this;
        }

        public Builder formatSymbolPosition(int formatSymbolPosition) {
            this.formatSymbolPosition = formatSymbolPosition;
            return this;
        }

        public Builder showFormatSymbol(boolean showFormatSymbol) {
            this.showFormatSymbol = showFormatSymbol;
            return this;
        }

        public Builder def(boolean def) {
            this.def = def;
            return this;
        }

        public Builder rate(BigDecimal rate) {
            this.rate = rate;
            return this;
        }

        public Builder direction(int direction) {
            this.direction = direction;
            return this;
        }

        public Builder useThousandSeparator(boolean useThousandSeparator) {
            this.useThousandSeparator = useThousandSeparator;
            return this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder isin(String isin) {
            this.isin = isin;
            return this;
        }

        public Builder registry(String registry) {
            this.registry = registry;
            return this;
        }

        public Builder created(long created) {
            this.created = created;
            return this;
        }

        public Builder modified(long modified) {
            this.modified = modified;
            return this;
        }
    }
}
