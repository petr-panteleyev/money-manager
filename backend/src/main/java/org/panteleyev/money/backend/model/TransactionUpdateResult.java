/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.model;

import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;

public record TransactionUpdateResult(
        Transaction transaction,
        Contact contact
) {
}
