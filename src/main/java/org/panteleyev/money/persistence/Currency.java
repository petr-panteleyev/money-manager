/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.persistence;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;

@Table("currency")
public class Currency implements Record {
    public static final class Builder {
        private int        id;
        private String     symbol;
        private String     description;
        private String     formatSymbol;
        private int        formatSymbolPosition;
        private boolean    showFormatSymbol;
        private boolean    def;
        private BigDecimal rate;
        private int        direction;
        private boolean    useThousandSeparator;

        public Builder() {
            formatSymbolPosition = 0;
            showFormatSymbol = false;
            def = false;
            rate = BigDecimal.ONE;
            direction = 0;
            useThousandSeparator = false;
        }

        public Builder(Currency c) {
            if (c != null) {
                this.id = c.getId();
                this.symbol = c.getSymbol();
                this.description = c.getDescription();
                this.formatSymbol = c.getFormatSymbol();
                this.formatSymbolPosition = c.getFormatSymbolPosition();
                this.showFormatSymbol = c.isShowFormatSymbol();
                this.def = c.isDef();
                this.rate = c.getRate();
                this.direction = c.getDirection();
                this.useThousandSeparator = c.isUseThousandSeparator();
            }
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public int id() {
            return id;
        }

        public Builder symbol(String x) {
            this.symbol = x;
            return this;
        }

        public Builder description(String x) {
            this.description = x;
            return this;
        }

        public Builder formatSymbol(String x) {
            this.formatSymbol = x;
            return this;
        }

        public Builder formatSymbolPosition(int x) {
            this.formatSymbolPosition = x;
            return this;
        }

        public Builder showFormatSymbol(boolean x) {
            this.showFormatSymbol = x;
            return this;
        }

        public Builder def(boolean x) {
            this.def = x;
            return this;
        }

        public Builder rate(BigDecimal x) {
            this.rate = x;
            return this;
        }

        public Builder direction(int x) {
            this.direction = x;
            return this;
        }

        public Builder useThousandSeparator(boolean x) {
            this.useThousandSeparator = x;
            return this;
        }

        public Currency build() {
            if (id == 0) {
                throw new IllegalStateException("Currency.id == 0");
            }
            Objects.requireNonNull(symbol);

            return new Currency(id,
                    symbol,
                    description,
                    formatSymbol,
                    formatSymbolPosition,
                    showFormatSymbol,
                    def,
                    rate,
                    direction,
                    useThousandSeparator
                );
        }
    }

    private final int        id;
    private final String     symbol;
    private final String     description;
    private final String     formatSymbol;
    private final int        formatSymbolPosition;
    private final boolean    showFormatSymbol;
    private final boolean    def;
    private final BigDecimal rate;
    private final int        direction;
    private final boolean    useThousandSeparator;

    @RecordBuilder
    public Currency(
            @Field(Field.ID) int id,
            @Field("symbol") String symbol,
            @Field("description") String description,
            @Field("format_symbol") String formatSymbol,
            @Field("format_symbol_position") int formatSymbolPosition,
            @Field("show_format_symbol") boolean showFormatSymbol,
            @Field("is_default") boolean def,
            @Field("rate") BigDecimal rate,
            @Field("direction") int direction,
            @Field("show_t_separator") boolean useThousandSeparator
    ) {
        this.id = id;
        this.symbol = symbol;
        this.description = description;
        this.formatSymbol = formatSymbol;
        this.formatSymbolPosition = formatSymbolPosition;
        this.showFormatSymbol = showFormatSymbol;
        this.def = def;
        this.rate = rate;
        this.direction = direction;
        this.useThousandSeparator = useThousandSeparator;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public int getId() {
        return id;
    }

    @Field("symbol")
    public String getSymbol() {
        return symbol;
    }

    @Field("description")
    public String getDescription() {
        return description;
    }

    @Field("format_symbol")
    public String getFormatSymbol() {
        return formatSymbol;
    }

    @Field("format_symbol_position")
    public int getFormatSymbolPosition() {
        return formatSymbolPosition;
    }

    @Field("show_format_symbol")
    public boolean isShowFormatSymbol() {
        return showFormatSymbol;
    }

    @Field("is_default")
    public boolean isDef() {
        return def;
    }

    @Field("rate")
    public BigDecimal getRate() {
        return rate;
    }

    @Field("direction")
    public int getDirection() {
        return direction;
    }

    @Field("show_t_separator")
    public boolean isUseThousandSeparator() {
        return useThousandSeparator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Currency) {
            Currency that = (Currency)obj;
            return this.id == that.id
                    && Objects.equals(this.symbol, that.symbol)
                    && Objects.equals(this.description, that.description)
                    && Objects.equals(this.formatSymbol, that.formatSymbol)
                    && this.formatSymbolPosition == that.formatSymbolPosition
                    && this.showFormatSymbol == that.showFormatSymbol
                    && this.def == that.def
                    && this.rate.compareTo(that.rate) == 0
                    && this.direction == that.direction
                    && this.useThousandSeparator == that.useThousandSeparator;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def,
                rate.stripTrailingZeros(), direction, useThousandSeparator);
    }
}
