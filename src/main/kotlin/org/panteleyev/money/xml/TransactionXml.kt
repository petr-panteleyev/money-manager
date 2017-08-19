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

import org.panteleyev.money.persistence.Transaction
import java.math.BigDecimal
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement

class TransactionXml() {
    @get:XmlAttribute(name = "id")
    var id: Int = 0

    @get:XmlElement(name = "amount")
    var amount: BigDecimal = BigDecimal.ZERO

    @get:XmlElement(name = "day")
    var day: Int = 0

    @get:XmlElement(name = "month")
    var month: Int = 0

    @get:XmlElement(name = "year")
    var year: Int = 0

    @get:XmlElement(name = "transactionTypeId")
    var transactionTypeId: Int = 0

    @get:XmlElement(name = "comment")
    var comment: String = ""

    @get:XmlElement(name = "checked")
    var checked: Boolean = false

    @get:XmlElement(name = "accountDebitedId")
    var accountDebitedId: Int = 0

    @get:XmlElement(name = "accountCreditedId")
    var accountCreditedId: Int = 0

    @get:XmlElement(name = "accountDebitedTypeId")
    var accountDebitedTypeId: Int = 0

    @get:XmlElement(name = "accountCreditedTypeId")
    var accountCreditedTypeId: Int = 0

    @get:XmlElement(name = "accountDebitedCategoryId")
    var accountDebitedCategoryId: Int = 0

    @get:XmlElement(name = "accountCreditedCategoryId")
    var accountCreditedCategoryId: Int = 0

    @get:XmlElement(name = "groupId")
    var groupId: Int = 0

    @get:XmlElement(name = "contactId")
    var contactId: Int = 0

    @get:XmlElement(name = "rate")
    var rate: BigDecimal = BigDecimal.ZERO

    @get:XmlElement(name = "rateDirection")
    var rateDirection: Int = 0

    @get:XmlElement(name = "invoiceNumber")
    var invoiceNumber: String = ""

    @get:XmlElement(name = "guid")
    var guid: String = ""

    @get:XmlElement(name = "modified")
    var modified: Long = 0L

    constructor(t: Transaction) : this() {
        this.id = t.id
        this.amount = t.amount
        this.day = t.day
        this.month = t.month
        this.year = t.year
        this.transactionTypeId = t.transactionTypeId
        this.comment = t.comment
        this.checked = t.checked
        this.accountDebitedId = t.accountDebitedId
        this.accountCreditedId = t.accountCreditedId
        this.accountDebitedTypeId = t.accountDebitedTypeId
        this.accountCreditedTypeId = t.accountCreditedTypeId
        this.accountDebitedCategoryId = t.accountDebitedCategoryId
        this.accountCreditedCategoryId = t.accountCreditedCategoryId
        this.groupId = t.groupId
        this.contactId = t.contactId
        this.rate = t.rate
        this.rateDirection = t.rateDirection
        this.invoiceNumber = t.invoiceNumber
        this.guid = t.guid
        this.modified = t.modified
    }

    fun toTransaction(): Transaction = Transaction(
            id = id,
            amount = amount,
            day = day,
            month = month,
            year = year,
            transactionTypeId = transactionTypeId,
            comment = comment,
            checked = checked,
            accountDebitedId = accountDebitedId,
            accountCreditedId = accountCreditedId,
            accountDebitedTypeId = accountDebitedTypeId,
            accountCreditedTypeId = accountCreditedTypeId,
            accountDebitedCategoryId = accountDebitedCategoryId,
            accountCreditedCategoryId = accountCreditedCategoryId,
            groupId = groupId,
            contactId = contactId,
            rate = rate,
            rateDirection = rateDirection,
            invoiceNumber = invoiceNumber,
            guid = guid,
            modified = modified
    )
}