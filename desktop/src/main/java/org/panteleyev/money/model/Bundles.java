/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.model;

import org.panteleyev.money.bundles.CategoryTypeBundle;
import org.panteleyev.money.bundles.ContactTypeBundle;
import org.panteleyev.money.bundles.TransactionTypeBundle;
import java.util.ResourceBundle;
import static java.util.ResourceBundle.getBundle;

interface Bundles {
    ResourceBundle CATEGORY_TYPE_BUNDLE = getBundle(CategoryTypeBundle.class.getCanonicalName());
    ResourceBundle CONTACT_TYPE_BUNDLE = getBundle(ContactTypeBundle.class.getCanonicalName());
    ResourceBundle TRANSACTION_TYPE_BUNDLE = getBundle(TransactionTypeBundle.class.getCanonicalName());
}
