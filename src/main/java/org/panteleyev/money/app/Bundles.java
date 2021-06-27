/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import org.panteleyev.money.bundles.TransactionPredicateBundle;
import java.util.ResourceBundle;

public interface Bundles {
    ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
        ResourceBundle.getBundle(TransactionPredicateBundle.class.getCanonicalName());

    ResourceBundle BUILD_INFO_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.buildInfo");
}
