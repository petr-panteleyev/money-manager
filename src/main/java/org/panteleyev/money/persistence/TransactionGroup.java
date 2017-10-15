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
import java.util.Objects;

@Table("transaction_group")
public final class TransactionGroup implements MoneyRecord {
    private final int id;
    private final int day;
    private final int month;
    private final int year;
    private final boolean expanded;
    private final String guid;
    private final long modified;

    @RecordBuilder
    public TransactionGroup(@Field("id") int id,
                            @Field("date_day") int day,
                            @Field("date_month") int month,
                            @Field("date_year") int year,
                            @Field("expanded") boolean expanded,
                            @Field("guid") String guid,
                            @Field("modified") long modified) {
        this.id = id;
        this.day = day;
        this.month = month;
        this.year = year;
        this.expanded = expanded;
        this.guid = guid;
        this.modified = modified;
    }

    public TransactionGroup copy(int newId) {
        return new TransactionGroup(newId, day, month, year, expanded, guid, modified);
    }

    @Field(value = "id", primaryKey = true)
    public int getId() {
        return this.id;
    }

    @Field("date_day")
    public final int getDay() {
        return this.day;
    }

    @Field("date_month")
    public final int getMonth() {
        return this.month;
    }

    @Field("date_year")
    public final int getYear() {
        return this.year;
    }

    @Field("expanded")
    public final boolean getExpanded() {
        return this.expanded;
    }

    @Field("guid")
    public String getGuid() {
        return this.guid;
    }

    @Field("modified")
    public long getModified() {
        return this.modified;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof TransactionGroup)) {
            return false;
        }

        TransactionGroup that = (TransactionGroup) other;

        return this.id == that.id
                && this.day == that.day
                && this.month == that.month
                && this.year == that.year
                && this.expanded == that.expanded
                && Objects.equals(this.guid, that.guid)
                && this.modified == that.modified;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, day, month, year, expanded, guid, modified);
    }
}
