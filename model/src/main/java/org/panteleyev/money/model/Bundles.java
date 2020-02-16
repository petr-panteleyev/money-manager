package org.panteleyev.money.model;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.ResourceBundle;

interface Bundles {
    ResourceBundle CATEGORY_TYPE_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.model.CategoryType");

    ResourceBundle CONTACT_TYPE_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.model.ContactType");

    ResourceBundle TRANSACTION_TYPE_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.model.TransactionType");
}
