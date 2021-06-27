/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

public class ContactTypeBundle extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][] {
            {"PERSONAL", "Personal"},
            {"CLIENT", "Client"},
            {"SUPPLIER", "Supplier"},
            {"EMPLOYEE", "Employee"},
            {"EMPLOYER", "Employer"},
            {"SERVICE", "Service"},
        };
    }
}
