/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
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
