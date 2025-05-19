/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Currency")
@Table(name = "currency")
public class CurrencyEntity implements MoneyEntity {
    private UUID uuid;
    private String symbol;
    private String description;
    private String formatSymbol;
    private int formatSymbolPosition;
    private boolean showFormatSymbol;
    private boolean def;
    private BigDecimal rate;
    private int rateDirection;
    private boolean useThousandSeparator;
    private long created;
    private long modified;

    public CurrencyEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public CurrencyEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public CurrencyEntity setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public CurrencyEntity setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getFormatSymbol() {
        return formatSymbol;
    }

    public CurrencyEntity setFormatSymbol(String formatSymbol) {
        this.formatSymbol = formatSymbol;
        return this;
    }

    @Column(name = "format_symbol_pos")
    public int getFormatSymbolPosition() {
        return formatSymbolPosition;
    }

    public CurrencyEntity setFormatSymbolPosition(int formatSymbolPosition) {
        this.formatSymbolPosition = formatSymbolPosition;
        return this;
    }

    public boolean isShowFormatSymbol() {
        return showFormatSymbol;
    }

    public CurrencyEntity setShowFormatSymbol(boolean showFormatSymbol) {
        this.showFormatSymbol = showFormatSymbol;
        return this;
    }

    public boolean isDef() {
        return def;
    }

    public CurrencyEntity setDef(boolean def) {
        this.def = def;
        return this;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public CurrencyEntity setRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    @Column(name = "rate_direction")
    public int getDirection() {
        return rateDirection;
    }

    public CurrencyEntity setDirection(int direction) {
        this.rateDirection = direction;
        return this;
    }

    @Column(name = "use_th_separator")
    public boolean isUseThousandSeparator() {
        return useThousandSeparator;
    }

    public CurrencyEntity setUseThousandSeparator(boolean useThousandSeparator) {
        this.useThousandSeparator = useThousandSeparator;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public CurrencyEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public CurrencyEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CurrencyEntity that)) return false;
        return formatSymbolPosition == that.formatSymbolPosition
                && showFormatSymbol == that.showFormatSymbol
                && def == that.def
                && rateDirection == that.rateDirection
                && useThousandSeparator == that.useThousandSeparator
                && created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(symbol, that.symbol)
                && Objects.equals(description, that.description)
                && Objects.equals(formatSymbol, that.formatSymbol)
                && Objects.equals(rate, that.rate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def, rate,
                rateDirection, useThousandSeparator, created, modified);
    }
}
