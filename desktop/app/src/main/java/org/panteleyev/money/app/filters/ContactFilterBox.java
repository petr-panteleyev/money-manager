/*
 Copyright © 2020-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.filters;

import javafx.scene.control.TextField;
import org.panteleyev.fx.FxFactory;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.model.Transaction;

import java.util.Optional;

import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.Predicates.contactByName;

public class ContactFilterBox {
    private final PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();
    private final TextField searchField = FxFactory.searchField(SEARCH_FIELD_FACTORY, this::updatePredicate);

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
