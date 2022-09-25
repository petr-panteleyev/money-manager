/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.bundles;

import java.util.ListResourceBundle;

import static org.panteleyev.money.model.DocumentType.BILL;
import static org.panteleyev.money.model.DocumentType.CONTRACT;
import static org.panteleyev.money.model.DocumentType.NOTIFICATION;
import static org.panteleyev.money.model.DocumentType.OTHER;
import static org.panteleyev.money.model.DocumentType.RECEIPT;
import static org.panteleyev.money.model.DocumentType.STATEMENT;

@SuppressWarnings("unused")
public class DocumentTypeBundle_ru_RU extends ListResourceBundle {
    @Override
    protected Object[][] getContents() {
        return new Object[][]{
                {BILL.name(), "Счет"},
                {CONTRACT.name(), "Договор"},
                {RECEIPT.name(), "Чек"},
                {STATEMENT.name(), "Выписка"},
                {NOTIFICATION.name(), "Уведомление"},
                {OTHER.name(), "Другое"},
        };
    }
}
