/*
 * Copyright (c) 2015, 2017, Petr Panteleyev <petr@panteleyev.org>
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

@Table("transaction_type")
public class TransactionType implements Record, Named, Comparable<TransactionType> {
    private final static ResourceBundle BUNDLE = ResourceBundle.getBundle("org.panteleyev.money.persistence.TransactionType");

    public static final int ID_CARD_PAYMENT = 1;
    public static final int ID_UNDEFINED    = 21;

    public static final Map<Integer, String> PREDEFINED = new HashMap<Integer, String>() {{
        put(ID_CARD_PAYMENT, "/Card Payment"); put(2, "/Cash Purchase"); put(3, "/Cheque");
        put(4, "-");
        put(5, "/Withdrawal"); put(6, "/Cashier"); put(7, "/Deposit"); put(8, "/Transfer");
        put(9, "-");
        put(10, "/Interest"); put(11, "/Dividend");
        put(12, "-");
        put(13, "/Direct Billing"); put(14, "/Charge"); put(15, "/Fee");
        put(16, "-");
        put(17, "/Income"); put(18, "/Sale");
        put(19, "-");
        put(20, "/Refund"); put(ID_UNDEFINED, "/Undefined");
    }};

    private final Integer id;
    private final String  name;

    @SuppressWarnings("FieldMayBeFinal")
    private String  translatedName;

    @RecordBuilder
    public TransactionType(
            @Field(Field.ID) Integer id,
            @Field("name") String name
    ) {
        this.id = id;
        this.name = name;

        try {
            translatedName = BUNDLE.getString(name);
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
    @Override
    public String getName() {
        return name;
    }

    public String getTranslatedName() {
        return translatedName;
    }

    @Override
    public int compareTo(TransactionType o) {
        return getTranslatedName().compareTo(o.getTranslatedName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof TransactionType) {
            TransactionType that = (TransactionType)obj;
            return Objects.equals(this.id, that.id)
                    && Objects.equals(this.name, that.name);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
