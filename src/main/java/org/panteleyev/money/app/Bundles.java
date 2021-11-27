/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import org.panteleyev.money.bundles.CategoryTypeBundle;
import org.panteleyev.money.bundles.ContactTypeBundle;
import org.panteleyev.money.bundles.TransactionTypeBundle;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.TransactionType;
import java.util.ResourceBundle;
import static java.util.ResourceBundle.getBundle;

public interface Bundles {
    ResourceBundle CATEGORY_TYPE_BUNDLE = getBundle(CategoryTypeBundle.class.getCanonicalName());
    ResourceBundle CONTACT_TYPE_BUNDLE = getBundle(ContactTypeBundle.class.getCanonicalName());
    ResourceBundle TRANSACTION_TYPE_BUNDLE = getBundle(TransactionTypeBundle.class.getCanonicalName());

    static String translate(CategoryType type) {
        return CATEGORY_TYPE_BUNDLE.getString(type.name() + "_name");
    }

    static String translate(ContactType type) {
        return CONTACT_TYPE_BUNDLE.getString(type.name());
    }

    static String translate(TransactionType type) {
        return TRANSACTION_TYPE_BUNDLE.getString(type.name());
    }
}
