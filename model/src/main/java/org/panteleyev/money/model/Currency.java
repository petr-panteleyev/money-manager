/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public record Currency(
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
        var sumString = value.abs().setScale(2, RoundingMode.HALF_UP).toString();
        var signString = value.signum() < 0 ? "-" : "";

        sumString = formatSymbolPosition == 0 ?
            signString + formatSymbol + sumString :
            signString + sumString + formatSymbol;

        return sumString;
    }

    public static String defaultFormatValue(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toString();
    }

    public static final class Builder {
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
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public Builder(Currency c) {
            if (c == null) {
                return;
            }

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
            created = c.created();
            modified = c.modified();
        }

        public Currency build() {


            return new Currency(uuid, symbol, description, formatSymbol, formatSymbolPosition,
                showFormatSymbol, def, rate, direction, useThousandSeparator, created, modified);
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
