package org.panteleyev.money.filters;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.scene.control.TextField;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.model.Transaction;
import java.util.Optional;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.money.Predicates.contactByName;
import static org.panteleyev.money.persistence.DataCache.cache;

public class ContactFilterBox {
    private final PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();
    private final TextField searchField = newSearchField(this::updatePredicate);

    public PredicateProperty<Transaction> predicateProperty() {
        return predicateProperty;
    }

    public TextField getTextField() {
        return searchField;
    }

    private void updatePredicate(String contactString) {
        if (contactString.isBlank()) {
            predicateProperty.reset();
        } else {
            predicateProperty.set(t -> Optional.ofNullable(t.contactUuid())
                .flatMap(uuid -> cache().getContact(uuid))
                .filter(contactByName(contactString))
                .isPresent());
        }
    }
}
