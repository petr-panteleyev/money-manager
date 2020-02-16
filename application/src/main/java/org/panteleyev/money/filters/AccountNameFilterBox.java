package org.panteleyev.money.filters;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TextField;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.Images;
import org.panteleyev.money.model.Account;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.money.Predicates.accountByName;

public class AccountNameFilterBox {
    private final PredicateProperty<Account> predicateProperty = new PredicateProperty<>();
    private final TextField searchField = newSearchField(Images.SEARCH, this::updatePredicate);

    public PredicateProperty<Account> predicateProperty() {
        return predicateProperty;
    }

    public TextField getTextField() {
        return searchField;
    }

    private void updatePredicate(String accountName) {
        predicateProperty.set(accountByName(accountName));
    }
}
