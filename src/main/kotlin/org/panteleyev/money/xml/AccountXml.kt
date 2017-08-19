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

import org.panteleyev.money.persistence.Account
import java.math.BigDecimal
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

class AccountXml() {
    @get:XmlAttribute(name = "id")
    var id : Int = 0

    @get:XmlElement(name = "name")
    var name : String = ""

    @get:XmlElement(name = "comment")
    var comment : String = ""

    @get:XmlElement(name = "openingBalance")
    var openingBalance : BigDecimal = BigDecimal.ZERO

    @get:XmlElement(name = "accountLimit")
    var accountLimit : BigDecimal = BigDecimal.ZERO

    @get:XmlElement(name = "currencyRate")
    var currencyRate : BigDecimal    = BigDecimal.ZERO

    @get:XmlElement(name = "typeId")
    var typeId : Int = 0

    @get:XmlElement(name = "categoryId")
    var categoryId : Int = 0

    @get:XmlElement(name = "currencyId")
    var currencyId : Int = 0

    @get:XmlElement(name = "enabled")
    var enabled : Boolean = false

    @get:XmlElement(name = "guid")
    var guid: String = ""

    @get:XmlElement(name = "modified")
    var modified: Long = 0L

    constructor(a : Account) : this() {
        this.id = a.id
        this.name = a.name
        this.comment = a.comment
        this.openingBalance = a.openingBalance
        this.accountLimit = a.accountLimit
        this.currencyRate = a.currencyRate
        this.typeId = a.typeId
        this.categoryId = a.categoryId
        this.currencyId = a.currencyId
        this.enabled = a.enabled
        this.guid = a.guid
        this.modified = a.modified
    }

    fun toAccount() = Account(
            id = id,
            name = name,
            comment = comment,
            openingBalance = openingBalance,
            accountLimit = accountLimit,
            currencyRate = currencyRate,
            typeId = typeId,
            categoryId = categoryId,
            currencyId = currencyId,
            enabled = enabled,
            guid = guid,
            modified = modified
    )
}