package org.panteleyev.money.model;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.Arrays;
import static org.panteleyev.money.model.Bundles.CONTACT_TYPE_BUNDLE;

public enum ContactType {
    PERSONAL,
    CLIENT,
    SUPPLIER,
    EMPLOYEE,
    EMPLOYER,
    SERVICE;

    private final String typeName;

    ContactType() {
        typeName = CONTACT_TYPE_BUNDLE.getString(name());
    }

    public String getTypeName() {
        return typeName;
    }
}
