package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.Arrays;
import static org.panteleyev.money.model.Bundles.CONTACT_TYPE_BUNDLE;

public enum ContactType {
    PERSONAL(1),
    CLIENT(2),
    SUPPLIER(3),
    EMPLOYEE(4),
    EMPLOYER(5),
    SERVICE(6);

    private final int id;
    private final String typeName;

    ContactType(int id) {
        this.id = id;
        typeName = CONTACT_TYPE_BUNDLE.getString("name" + id);
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
