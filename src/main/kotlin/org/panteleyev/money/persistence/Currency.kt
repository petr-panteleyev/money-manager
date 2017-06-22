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

import org.panteleyev.persistence.Record
import org.panteleyev.persistence.annotations.Field
import org.panteleyev.persistence.annotations.RecordBuilder
import org.panteleyev.persistence.annotations.Table
import java.math.BigDecimal
import java.util.Objects

@Table("currency")
data class Currency @RecordBuilder constructor (
        @Field(Field.ID)
        val _id : Int,

        @param:Field("symbol")
        @get:Field("symbol")
        val symbol : String,

        @param:Field("description")
        @get:Field("description")
        val description : String,

        @param:Field("format_symbol")
        @get:Field("format_symbol")
        val formatSymbol : String,

        @param:Field("format_symbol_position")
        @get:Field("format_symbol_position")
        val formatSymbolPosition : Int,

        @param:Field("show_format_symbol")
        @get:Field("show_format_symbol")
        val showFormatSymbol : Boolean,

        @param:Field("is_default")
        @get:Field("is_default")
        val def : Boolean,

        @param:Field("rate")
        @get:Field("rate")
        val rate : BigDecimal,

        @param:Field("direction")
        @get:Field("direction")
        val direction : Int,

        @param:Field("show_t_separator")
        @get:Field("show_t_separator")
        val useThousandSeparator : Boolean
) : Record {
    @Field(value = Field.ID, primaryKey = true)
    override fun getId(): Int = _id

    override fun equals(other: Any?): Boolean {
        return if (other is Currency) {
            this._id == other._id
                && this.symbol == other.symbol
                && this.description == other.description
                && this.formatSymbol == other.formatSymbol
                && this.formatSymbolPosition == other.formatSymbolPosition
                && this.showFormatSymbol == other.showFormatSymbol
                && this.def == other.def
                && this.rate.compareTo(other.rate) == 0
                && this.direction == other.direction
                && this.useThousandSeparator == other.useThousandSeparator
        } else {
            false
        }
    }

    override fun hashCode(): Int =
        Objects.hash(_id, symbol, description, formatSymbol, formatSymbolPosition, showFormatSymbol,
                def, rate.stripTrailingZeros(), direction, useThousandSeparator)
}