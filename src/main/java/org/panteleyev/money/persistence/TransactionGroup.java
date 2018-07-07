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
import java.util.Objects;

@Table("transaction_group")
public final class TransactionGroup implements MoneyRecord {
    @Column(value = "id", primaryKey = true)
    private final int id;
    @Column("date_day")
    private final int day;
    @Column("date_month")
    private final int month;
    @Column("date_year")
    private final int year;
    @Column("expanded")
    private final boolean expanded;
    @Column("guid")
    private final String guid;
    @Column("modified")
    private final long modified;

    @RecordBuilder
    public TransactionGroup(@Column("id") int id,
                            @Column("date_day") int day,
                            @Column("date_month") int month,
                            @Column("date_year") int year,
                            @Column("expanded") boolean expanded,
                            @Column("guid") String guid,
                            @Column("modified") long modified) {
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

    public int getId() {
        return this.id;
    }

    public int getDay() {
        return this.day;
    }

    public int getMonth() {
        return this.month;
    }

    public int getYear() {
        return this.year;
    }

    public boolean getExpanded() {
        return this.expanded;
    }

    public String getGuid() {
        return this.guid;
    }

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
