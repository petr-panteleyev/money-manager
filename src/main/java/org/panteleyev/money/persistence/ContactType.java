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
import java.util.Objects;
import org.panteleyev.persistence.Record;
import org.panteleyev.persistence.annotations.Field;
import org.panteleyev.persistence.annotations.RecordBuilder;
import org.panteleyev.persistence.annotations.Table;

@Table("contact_type")
public class ContactType implements Record, Named {
    public static final int ID_PERSONAL = 1;
    public static final int ID_CLIENT   = 2;
    public static final int ID_SUPPLIER = 3;
    public static final int ID_EMPLOYEE = 4;
    public static final int ID_EMPLOYER = 5;
    public static final int ID_SERVICE  = 6;

    public static final Map<Integer, String> PREDEFINED = new HashMap<Integer, String>() {{
        put(ID_PERSONAL, "/personal");
        put(ID_CLIENT, "/client");
        put(ID_SUPPLIER, "/supplier");
        put(ID_EMPLOYEE, "/employee");
        put(ID_EMPLOYER, "/employer");
        put(ID_SERVICE, "/service");
    }};

    private final int    id;
    private final String name;

    @RecordBuilder
    public ContactType(@Field(Field.ID) Integer id, @Field("name") String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof ContactType) {
            ContactType that = (ContactType)obj;

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
