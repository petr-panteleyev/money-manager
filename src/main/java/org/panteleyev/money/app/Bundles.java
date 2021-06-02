/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import java.util.ResourceBundle;

public interface Bundles {
    ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.app.TransactionPredicate");

    ResourceBundle BUILD_INFO_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.app.res.buildInfo");
}
