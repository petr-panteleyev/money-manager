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

package org.panteleyev.money.xml

import org.panteleyev.money.persistence.Currency
import java.math.BigDecimal
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

class CurrencyXml() {
    @get:XmlAttribute(name = "id")
    var id: Int = 0

    @get:XmlElement(name = "symbol")
    var symbol: String = ""

    @get:XmlElement(name = "description")
    var description: String = ""

    @get:XmlElement(name = "formatSymbol")
    var formatSymbol: String = ""

    @get:XmlElement(name = "formatSymbolPosition")
    var formatSymbolPosition: Int = 0

    @get:XmlElement(name = "showFormatSymbol")
    var showFormatSymbol: Boolean = false

    @get:XmlElement(name = "default")
    var def: Boolean = false

    @get:XmlElement(name = "rate")
    var rate: BigDecimal = BigDecimal.ZERO

    @get:XmlElement(name = "direction")
    var direction: Int = 0

    @get:XmlElement(name = "useThousandSeparator")
    var useThousandSeparator: Boolean = false

    @get:XmlElement(name = "guid")
    var guid: String = ""

    @get:XmlElement(name = "modified")
    var modified: Long = 0L

    constructor(c: Currency) : this() {
        this.id = c.id
        this.symbol = c.symbol
        this.description = c.description
        this.formatSymbol = c.formatSymbol
        this.formatSymbolPosition = c.formatSymbolPosition
        this.showFormatSymbol = c.showFormatSymbol
        this.def = c.def
        this.rate = c.rate
        this.direction = c.direction
        this.useThousandSeparator = c.useThousandSeparator
        this.guid = c.guid
        this.modified = c.modified
    }

    fun toCurrency(): Currency = Currency(
            id = id,
            symbol = symbol,
            description = description,
            formatSymbol = formatSymbol,
            formatSymbolPosition = formatSymbolPosition,
            showFormatSymbol = showFormatSymbol,
            def = def,
            rate = rate,
            direction = direction,
            useThousandSeparator = useThousandSeparator,
            guid = guid,
            modified = modified
    )
}