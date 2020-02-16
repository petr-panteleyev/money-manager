package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.util.ResourceBundle;

public interface Bundles {
    ResourceBundle TRANSACTION_PREDICATE_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.TransactionPredicate");

    ResourceBundle BUILD_INFO_BUNDLE =
        ResourceBundle.getBundle("org.panteleyev.money.res.buildInfo");
}
