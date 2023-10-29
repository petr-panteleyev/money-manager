/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.contact;

import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableView;
import org.panteleyev.money.app.cells.ContactNameCell;
import org.panteleyev.money.model.Contact;

import java.util.Comparator;
import java.util.List;

import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.money.app.Bundles.translate;

final class ContactTableView extends TableView<Contact> {
    public ContactTableView(SortedList<Contact> list) {
        super(list);

        var w = widthProperty().subtract(20);
        getColumns().setAll(List.of(
                tableObjectColumn("Имя", b ->
                        b.withCellFactory(x -> new ContactNameCell())
                                .withComparator(Comparator.comparing(Contact::name))
                                .withWidthBinding(w.multiply(0.4))),
                tableColumn("Тип", b ->
                        b.withPropertyCallback((Contact p) -> translate(p.type())).withWidthBinding(w.multiply(0.2))),
                tableColumn("Телефон", b ->
                        b.withPropertyCallback(Contact::phone).withWidthBinding(w.multiply(0.2))),
                tableColumn("E-Mail", b ->
                        b.withPropertyCallback(Contact::email).withWidthBinding(w.multiply(0.2)))
        ));
        list.comparatorProperty().bind(comparatorProperty());
        getSortOrder().add(getColumns().getFirst());
        sort();
    }
}
