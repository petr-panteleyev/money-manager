/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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

import java.util.Objects;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;

@Table("transaction_group")
public class TransactionGroup implements Record {
    private final Integer id;
    private final Integer day;
    private final Integer month;
    private final Integer year;
    private final Boolean expanded;

    public static class Builder {
        private Integer id;
        private Integer day;
        private Integer month;
        private Integer year;
        private boolean expanded;

        public Builder() {
        }

        public Builder(TransactionGroup g) {
            this.id = g.getId();
            this.day = g.getDay();
            this.month = g.getMonth();
            this.year = g.getYear();
            this.expanded = g.isExpanded();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder day(Integer day) {
            this.day = day;
            return this;
        }

        public Builder month(Integer month) {
            this.month = month;
            return this;
        }

        public Builder year(Integer year) {
            this.year = year;
            return this;
        }

        public Builder expanded(Boolean expanded) {
            this.expanded = expanded;
            return this;
        }

        public TransactionGroup build() {
            return new TransactionGroup(id, day, month, year, expanded);
        }
    }


    @RecordBuilder
    public TransactionGroup(
            @Field("id")         int id,
            @Field("date_day")   int day,
            @Field("date_month") int month,
            @Field("date_year")  int year,
            @Field("expanded")   boolean expanded
    ) {
        this.id = id;
        this.day = day;
        this.month = month;
        this.year = year;
        this.expanded = expanded;
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public Integer getId() {
        return id;
    }

    @Field("date_day")
    public int getDay() {
        return day;
    }

    @Field("date_month")
    public int getMonth() {
        return month;
    }

    @Field("date_year")
    public int getYear() {
        return year;
    }

    @Field("expanded")
    public boolean isExpanded() {
        return expanded;
    }

    public TransactionGroup expand(boolean expand) {
        return new TransactionGroup(id, day, month, year, expand);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof TransactionGroup) {
            TransactionGroup that = (TransactionGroup)obj;

            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.day, that.day)
                    && Objects.equals(this.month, that.month)
                    && Objects.equals(this.year, that.year)
                    && Objects.equals(this.expanded, that.expanded);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, day, month, year, expanded);
    }
}
