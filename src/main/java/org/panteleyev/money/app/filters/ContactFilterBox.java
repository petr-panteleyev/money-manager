/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app.filters;

import javafx.scene.control.TextField;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.model.Transaction;
import java.util.Optional;
import static org.panteleyev.fx.FxFactory.newSearchField;
import static org.panteleyev.money.app.Constants.SEARCH_FIELD_FACTORY;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.Predicates.contactByName;

public class ContactFilterBox {
    private final PredicateProperty<Transaction> predicateProperty = new PredicateProperty<>();
    private final TextField searchField = newSearchField(SEARCH_FIELD_FACTORY, this::updatePredicate);

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
