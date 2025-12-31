// Copyright © 2023-2025 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.contact;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;

import java.util.Comparator;
import java.util.List;

import static org.panteleyev.functional.Scope.apply;
import static org.panteleyev.fx.factories.TableFactory.tableObjectColumn;
import static org.panteleyev.fx.factories.TableFactory.tableStringColumn;
import static org.panteleyev.money.app.Bundles.translate;

final class ContactTableView extends TableView<Contact> {
    public ContactTableView(SortedList<Contact> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                apply(tableObjectColumn("Имя"), c -> {
                    c.setCellFactory(_ -> new ContactNameCell());
                    c.comparator(Comparator.comparing(Contact::name));
                    c.widthBinding(w.multiply(0.4));
                }),
                apply(tableStringColumn("Тип"), c -> {
                    c.valueConverter(p -> translate(p.type()));
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableStringColumn("Телефон"), c -> {
                    c.valueConverter(Contact::phone);
                    c.widthBinding(w.multiply(0.2));
                }),
                apply(tableStringColumn("E-Mail"), c -> {
                    c.valueConverter(Contact::email);
                    c.widthBinding(w.multiply(0.2));
                })
        ));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(getColumns().getFirst());
        sort();
    }
}
