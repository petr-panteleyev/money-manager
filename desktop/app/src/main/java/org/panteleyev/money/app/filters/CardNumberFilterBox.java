// Copyright © 2019-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.filters;

import javafx.scene.control.TextField;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.fx.factories.TextFieldFactory;
import org.panteleyev.money.model.Card;

import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.Predicates.cardByNumber;

public class CardNumberFilterBox {
    private final PredicateProperty<Card> predicateProperty = new PredicateProperty<>();
    private final TextField searchField = TextFieldFactory.searchField(SEARCH_FIELD_FACTORY, this::updatePredicate);

    public PredicateProperty<Card> predicateProperty() {
        return predicateProperty;
    }

    public TextField getTextField() {
        return searchField;
    }

    private void updatePredicate(String cardNumber) {
        predicateProperty.set(cardByNumber(cardNumber));
    }
}
