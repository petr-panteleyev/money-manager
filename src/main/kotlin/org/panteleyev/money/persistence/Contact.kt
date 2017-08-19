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

package org.panteleyev.money.persistence

import org.panteleyev.persistence.annotations.Field
import org.panteleyev.persistence.annotations.RecordBuilder
import org.panteleyev.persistence.annotations.Table
import java.util.UUID

@Table("contact")
data class Contact @RecordBuilder constructor (
        @param:Field("id")
        @get:Field(value = "id", primaryKey = true)
        override val id : Int,

        @param:Field("name")
        @get:Field("name")
        override val name : String,

        @param:Field("type_id")
        @get:Field("type_id")
        val typeId : Int,

        @param:Field("phone")
        @get:Field("phone")
        val phone : String?,

        @param:Field("mobile")
        @get:Field("mobile")
        val mobile : String?,

        @param:Field("email")
        @get:Field("email")
        val email : String?,

        @param:Field("web")
        @get:Field("web")
        val web : String?,

        @param:Field("comment")
        @get:Field("comment")
        val comment : String?,

        @param:Field("street")
        @get:Field("street")
        val street : String?,

        @param:Field("city")
        @get:Field("city")
        val city : String?,

        @param:Field("country")
        @get:Field("country")
        val country : String?,

        @param:Field("zip")
        @get:Field("zip")
        val zip : String?,

        @param:Field("guid")
        @get:Field("guid")
        override val guid: String,

        @param:Field("modified")
        @get:Field("modified")
        override val modified: Long
) : MoneyRecord, Named, Comparable<Contact> {
    val type = ContactType.get(typeId)

    constructor(id: Int, name: String) : this(id, name, ContactType.PERSONAL.id,
            "", "", "", "", "", "", "", "", "", UUID.randomUUID().toString(),
            System.currentTimeMillis()
    )

    override fun compareTo(other: Contact): Int = name.compareTo(other.name, true)
}