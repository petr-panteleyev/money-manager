/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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

import org.panteleyev.persistence.annotations.Column;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Table("currency")
public final class Currency implements MoneyRecord {
    @Column(value = "id", primaryKey = true)
    private final int id;
    @Column("symbol")
    private final String symbol;
    @Column("description")
    private final String description;
    @Column("format_symbol")
    private final String formatSymbol;
    @Column("format_symbol_position")
    private final int formatSymbolPosition;
    @Column("show_format_symbol")
    private final boolean showFormatSymbol;
    @Column("is_default")
    private final boolean def;
    @Column("rate")
    private final BigDecimal rate;
    @Column("direction")
    private final int direction;
    @Column("show_t_separator")
    private final boolean useThousandSeparator;
    @Column("guid")
    private final String guid;
    @Column("modified")
    private final long modified;

    @RecordBuilder
    public Currency(@Column("id") int id,
                    @Column("symbol") String symbol,
                    @Column("description") String description,
                    @Column("format_symbol") String formatSymbol,
                    @Column("format_symbol_position") int formatSymbolPosition,
                    @Column("show_format_symbol") boolean showFormatSymbol,
                    @Column("is_default") boolean def,
                    @Column("rate") BigDecimal rate,
                    @Column("direction") int direction,
                    @Column("show_t_separator") boolean useThousandSeparator,
                    @Column("guid") String guid,
                    @Column("modified") long modified) {
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
    public int getId() {
        return this.id;
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

    @Override
    public String getGuid() {
        return this.guid;
    }

    @Override
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
