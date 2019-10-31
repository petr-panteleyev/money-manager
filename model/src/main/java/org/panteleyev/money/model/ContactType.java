/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.ResourceBundle;

public enum ContactType {
    PERSONAL(1),
    CLIENT(2),
    SUPPLIER(3),
    EMPLOYEE(4),
    EMPLOYER(5),
    SERVICE(6);

    private static final String BUNDLE_NAME = "org.panteleyev.money.model.ContactType";

    private final int id;
    private final String typeName;

    ContactType(int id) {
        this.id = id;

        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME);
        typeName = bundle.getString("name" + id);
    }

    public int getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }

    public static ContactType get(int id) {
        return Arrays.stream(ContactType.values())
                .filter(t -> t.id == id)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);

    }
}
