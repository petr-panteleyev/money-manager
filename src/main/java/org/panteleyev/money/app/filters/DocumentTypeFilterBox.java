/*
 Copyright (C) 2022 Petr Panteleyev

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

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.ToStringConverter;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.MoneyDocument;

import static org.panteleyev.money.app.Constants.ALL_TYPES_STRING;

public class DocumentTypeFilterBox {
    private final PredicateProperty<MoneyDocument> predicateProperty = new PredicateProperty<>();

    private final ChoiceBox<Object> typeBox = new ChoiceBox<>();

    public DocumentTypeFilterBox() {
        typeBox.getItems().addAll(
                ALL_TYPES_STRING,
                new Separator()
        );
        typeBox.getItems().addAll(
                DocumentType.values()
        );
        typeBox.setConverter(new ToStringConverter<>() {
            @Override
            public String toString(Object o) {
                if (o instanceof DocumentType type) {
                    return Bundles.translate(type);
                } else if (o instanceof String str) {
                    return str;
                } else {
                    return "";
                }
            }
        });
        typeBox.setOnAction(this::typeHandler);
        typeBox.getSelectionModel().selectFirst();
    }

    public PredicateProperty<MoneyDocument> predicateProperty() {
        return predicateProperty;
    }

    public Node getNode() {
        return typeBox;
    }

    private void typeHandler(ActionEvent event) {
        var selected = typeBox.getSelectionModel().getSelectedItem();
        if (selected instanceof DocumentType type) {
            predicateProperty.set(doc -> doc.documentType() == type);
        } else {
            predicateProperty.reset();
        }
    }
}
