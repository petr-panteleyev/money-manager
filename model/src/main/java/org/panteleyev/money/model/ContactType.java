// Copyright © 2017-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.List;

public enum ContactType {
    PERSONAL,
    CLIENT,
    SUPPLIER,
    EMPLOYEE,
    EMPLOYER,
    SERVICE;


    public static List<ContactType> asList() {
        return Arrays.asList(values());
    }
}
