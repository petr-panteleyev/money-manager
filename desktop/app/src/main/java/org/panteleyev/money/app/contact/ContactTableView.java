// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.contact;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;

import java.util.Comparator;
import java.util.List;

import static org.panteleyev.money.app.Bundles.translate;

final class ContactTableView extends TableView<Contact> {
    public ContactTableView(SortedList<Contact> list) {
        super(list);

        var w = widthProperty().subtract(20);

        var nameColumn = TableFactory.<Contact>tableObjectColumn("Имя");
        nameColumn.setCellFactory(_ -> new ContactNameCell());
        nameColumn.comparator(Comparator.comparing(Contact::name));
        nameColumn.widthBinding(w.multiply(0.4));
        var typeColumn = TableFactory.<Contact>tableStringColumn("Тип");
        typeColumn.valueConverter(p -> translate(p.type()));
        typeColumn.widthBinding(w.multiply(0.2));
        var phoneColumn = TableFactory.<Contact>tableStringColumn("Телефон");
        phoneColumn.valueConverter(Contact::phone);
        phoneColumn.widthBinding(w.multiply(0.2));
        var emailColumn = TableFactory.<Contact>tableStringColumn("E-Mail");
        emailColumn.valueConverter(Contact::email);
        emailColumn.widthBinding(w.multiply(0.2));

        getColumns().setAll(List.of(nameColumn, typeColumn, phoneColumn, emailColumn));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(nameColumn);
        sort();
    }
}
