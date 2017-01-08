/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;

@Table("cat_type")
public class CategoryType implements Record {
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle("org.panteleyev.money.persistence.CategoryType");

    public static final int BANKS_AND_CASH_ID   = 1;
    public static final int INCOMES_ID          = 2;
    public static final int EXPENSES_ID         = 3;
    public static final int DEBTS_ID            = 4;
    public static final int PORTFOLIO_ID        = 5;
    public static final int ASSETS_ID           = 6;
    public static final int STARTUP_ID          = 7;

    public static final Map<Integer, String> PREDEFINED = new HashMap<Integer, String>() {{
        put(BANKS_AND_CASH_ID, "/Banks &amp; cash");
        put(INCOMES_ID, "/Incomes");
        put(EXPENSES_ID, "/Expenses");
        put(DEBTS_ID, "/Debts");
        put(PORTFOLIO_ID, "/Portfolio");
        put(ASSETS_ID, "/Assets");
        put(STARTUP_ID, "/Startup");
    }};

    private final Integer id;
    private final String name;
    private final String comment;

    @SuppressWarnings("FieldMayBeFinal")
    private String translatedName;

    @RecordBuilder
    public CategoryType(
            @Field(Field.ID)  Integer id,
            @Field("name")    String name,
            @Field("comment") String comment
    ) {
        this.id = id;
        this.name = name;
        this.comment = comment;

        try {
            translatedName = (name == null)? null : BUNDLE.getString(name);
        } catch (MissingResourceException ex) {
            translatedName = name;
        }
    }

    @Field(value = Field.ID, primaryKey = true)
    @Override
    public Integer getId() {
        return id;
    }

    @Field("name")
    public String getName() {
        return name;
    }

    @Field("comment")
    public String getComment() {
        return comment;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof CategoryType) {
            CategoryType that = (CategoryType)obj;

            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.name, that.name)
                    && Objects.equals(this.comment, that.comment);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, comment);
    }
}
