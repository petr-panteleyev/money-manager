/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Table("currency")
public final class Currency implements MoneyRecord {
    private final int id;
    private final String symbol;
    private final String description;
    private final String formatSymbol;
    private final int formatSymbolPosition;
    private final boolean showFormatSymbol;
    private final boolean def;
    private final BigDecimal rate;
    private final int direction;
    private final boolean useThousandSeparator;
    private final String guid;
    private final long modified;

    @RecordBuilder
    public Currency(@Field("id") int id,
                    @Field("symbol") String symbol,
                    @Field("description") String description,
                    @Field("format_symbol") String formatSymbol,
                    @Field("format_symbol_position") int formatSymbolPosition,
                    @Field("show_format_symbol") boolean showFormatSymbol,
                    @Field("is_default") boolean def,
                    @Field("rate") BigDecimal rate,
                    @Field("direction") int direction,
                    @Field("show_t_separator") boolean useThousandSeparator,
                    @Field("guid") String guid,
                    @Field("modified") long modified) {
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
        this.guid = guid;
        this.modified = modified;
    }

    public Currency copy(int newId) {
        return new Currency(newId, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def,
                rate, direction, useThousandSeparator, guid, modified);
    }

    public Currency setDescription(String newDescription) {
        return new Currency(id, symbol, newDescription, formatSymbol, formatSymbolPosition, showFormatSymbol, def,
                rate, direction, useThousandSeparator, guid, modified);
    }

    @Override
    @Field(value = "id", primaryKey = true)
    public int getId() {
        return this.id;
    }

    @Field("symbol")
    public String getSymbol() {
        return this.symbol;
    }

    @Field("description")
    public String getDescription() {
        return this.description;
    }

    @Field("format_symbol")
    public String getFormatSymbol() {
        return this.formatSymbol;
    }

    @Field("format_symbol_position")
    public int getFormatSymbolPosition() {
        return this.formatSymbolPosition;
    }

    @Field("show_format_symbol")
    public boolean getShowFormatSymbol() {
        return this.showFormatSymbol;
    }

    @Field("is_default")
    public boolean getDef() {
        return this.def;
    }

    @Field("rate")
    public BigDecimal getRate() {
        return this.rate;
    }

    @Field("direction")
    public int getDirection() {
        return this.direction;
    }

    @Field("show_t_separator")
    public boolean getUseThousandSeparator() {
        return this.useThousandSeparator;
    }

    @Override
    @Field("guid")

    public String getGuid() {
        return this.guid;
    }

    @Override
    @Field("modified")
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

        return id == that.id
                && Objects.equals(symbol, that.symbol)
                && Objects.equals(description, that.description)
                && Objects.equals(formatSymbol, that.formatSymbol)
                && formatSymbolPosition == that.formatSymbolPosition
                && showFormatSymbol == that.showFormatSymbol
                && def == that.def
                && rate.compareTo(that.rate) == 0
                && direction == that.direction
                && useThousandSeparator == that.useThousandSeparator
                && Objects.equals(guid, that.guid)
                && modified == that.modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def,
                rate.stripTrailingZeros(), direction, useThousandSeparator, guid, modified);
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
}
