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

package org.panteleyev.money.xml;

import org.panteleyev.money.persistence.Currency;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.math.BigDecimal;

public final class CurrencyXml {
    private int id;
    private String symbol;
    private String description;
    private String formatSymbol;
    private int formatSymbolPosition;
    private boolean showFormatSymbol;
    private boolean def;
    private BigDecimal rate;
    private int direction;
    private boolean useThousandSeparator;
    private String guid;
    private long modified;

    public CurrencyXml() {
    }

    public CurrencyXml(Currency c) {
        id = c.getId();
        symbol = c.getSymbol();
        description = c.getDescription();
        formatSymbol = c.getFormatSymbol();
        formatSymbolPosition = c.getFormatSymbolPosition();
        showFormatSymbol = c.getShowFormatSymbol();
        def = c.getDef();
        rate = c.getRate();
        direction = c.getDirection();
        useThousandSeparator = c.getUseThousandSeparator();
        guid = c.getGuid();
        modified = c.getModified();
    }

    @XmlAttribute(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement(name = "symbol")
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @XmlElement(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement(name = "formatSymbol")
    public String getFormatSymbol() {
        return formatSymbol;
    }

    public void setFormatSymbol(String formatSymbol) {
        this.formatSymbol = formatSymbol;
    }

    @XmlElement(name = "formatSymbolPosition")
    public int getFormatSymbolPosition() {
        return formatSymbolPosition;
    }

    public void setFormatSymbolPosition(int formatSymbolPosition) {
        this.formatSymbolPosition = formatSymbolPosition;
    }

    @XmlElement(name = "showFormatSymbol")
    public boolean getShowFormatSymbol() {
        return showFormatSymbol;
    }

    public void setShowFormatSymbol(boolean showFormatSymbol) {
        this.showFormatSymbol = showFormatSymbol;
    }

    @XmlElement(name = "default")
    public boolean getDef() {
        return def;
    }

    public void setDef(boolean def) {
        this.def = def;
    }

    @XmlElement(name = "rate")
    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    @XmlElement(name = "direction")
    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    @XmlElement(name = "useThousandSeparator")
    public boolean getUseThousandSeparator() {
        return useThousandSeparator;
    }

    public void setUseThousandSeparator(boolean useThousandSeparator) {
        this.useThousandSeparator = useThousandSeparator;
    }

    @XmlElement(name = "guid")
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @XmlElement(name = "modified")
    public long getModified() {
        return modified;
    }

    public void setModified(long modified) {
        this.modified = modified;
    }

    public Currency toCurrency() {
        return new Currency(id, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol, def, rate,
                direction, useThousandSeparator, guid, modified);
    }
}
