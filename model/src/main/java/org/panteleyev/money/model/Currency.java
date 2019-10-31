/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.model;

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.PrimaryKey;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Table("currency")
public final class Currency implements MoneyRecord {
    @PrimaryKey
    @Column("uuid")
    private final UUID guid;
    @Column("symbol")
    private final String symbol;
    @Column("description")
    private final String description;
    @Column("format_symbol")
    private final String formatSymbol;
    @Column("format_symbol_pos")
    private final int formatSymbolPosition;
    @Column("show_format_symbol")
    private final boolean showFormatSymbol;
    @Column("def")
    private final boolean def;
    @Column("rate")
    private final BigDecimal rate;
    @Column("rate_direction")
    private final int direction;
    @Column("use_th_separator")
    private final boolean useThousandSeparator;
    @Column("created")
    private final long created;
    @Column("modified")
    private final long modified;

    @RecordBuilder
    public Currency(@Column("symbol") String symbol,
                    @Column("description") String description,
                    @Column("format_symbol") String formatSymbol,
                    @Column("format_symbol_pos") int formatSymbolPosition,
                    @Column("show_format_symbol") boolean showFormatSymbol,
                    @Column("def") boolean def,
                    @Column("rate") BigDecimal rate,
                    @Column("rate_direction") int direction,
                    @Column("use_th_separator") boolean useThousandSeparator,
                    @Column("uuid") UUID guid,
                    @Column("created") long created,
                    @Column("modified") long modified)
    {
        this.symbol = symbol;
        this.description = description;
        this.formatSymbol = formatSymbol;
        this.formatSymbolPosition = formatSymbolPosition;
        this.showFormatSymbol = showFormatSymbol;
        this.def = def;
        this.rate = rate;
        this.direction = direction;
        this.useThousandSeparator = useThousandSeparator;
        this.guid = guid;
        this.created = created;
        this.modified = modified;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFormatSymbol() {
        return this.formatSymbol;
    }

    public int getFormatSymbolPosition() {
        return this.formatSymbolPosition;
    }

    public boolean getShowFormatSymbol() {
        return this.showFormatSymbol;
    }

    public boolean getDef() {
        return this.def;
    }

    public BigDecimal getRate() {
        return this.rate;
    }

    public int getDirection() {
        return this.direction;
    }

    public boolean getUseThousandSeparator() {
        return this.useThousandSeparator;
    }

    public UUID getUuid() {
        return this.guid;
    }

    public long getCreated() {
        return created;
    }

    public long getModified() {
        return this.modified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Currency)) {
            return false;
        }

        Currency that = (Currency) other;
        return Objects.equals(symbol, that.symbol)
            && Objects.equals(description, that.description)
            && Objects.equals(formatSymbol, that.formatSymbol)
            && formatSymbolPosition == that.formatSymbolPosition
            && showFormatSymbol == that.showFormatSymbol
            && def == that.def
            && rate.compareTo(that.rate) == 0
            && direction == that.direction
            && useThousandSeparator == that.useThousandSeparator
            && Objects.equals(guid, that.guid)
            && created == that.created
            && modified == that.modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def,
            rate.stripTrailingZeros(), direction, useThousandSeparator, guid, created, modified);
    }

    public String formatValue(BigDecimal value) {
        String sumString = value.abs().setScale(2, RoundingMode.HALF_UP).toString();
        String signString = value.signum() < 0 ? "-" : "";

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
        private UUID guid = null;
        private long created = 0;
        private long modified = 0;

        public Builder() {
        }

        public Builder(Currency c) {
            if (c == null) {
                return;
            }

            symbol = c.getSymbol();
            description = c.getDescription();
            formatSymbol = c.getFormatSymbol();
            formatSymbolPosition = c.getFormatSymbolPosition();
            showFormatSymbol = c.getShowFormatSymbol();
            def = c.getDef();
            rate = c.getRate();
            direction = c.getDirection();
            useThousandSeparator = c.getUseThousandSeparator();
            guid = c.getUuid();
            created = c.getCreated();
            modified = c.getModified();
        }

        public Currency build() {
            if (guid == null) {
                guid = UUID.randomUUID();
            }

            long now = System.currentTimeMillis();
            if (created == 0) {
                created = now;
            }
            if (modified == 0) {
                modified = now;
            }

            return new Currency(symbol, description, formatSymbol, formatSymbolPosition,
                showFormatSymbol, def, rate, direction, useThousandSeparator, guid, created, modified);
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol == null ? "" : symbol;
            return this;
        }

        public Builder description(String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        public Builder formatSymbol(String formatSymbol) {
            this.formatSymbol = formatSymbol == null ? "" : formatSymbol;
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

        public Builder guid(UUID guid) {
            this.guid = guid;
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
