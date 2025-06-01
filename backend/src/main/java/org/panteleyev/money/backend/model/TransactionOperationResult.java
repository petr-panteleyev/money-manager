/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.model;

import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;

import java.util.Collection;

/**
 * Represents result of inserting or updating transaction.
 * This includes accounts affected by this operation and optionally created contact.
 *
 * @param transaction created or updated transaction
 * @param contact     created contact or null if none
 * @param accounts    affected accounts, at least one
 */
public record TransactionOperationResult(
        Transaction transaction,
        Contact contact,
        Collection<Account> accounts
) {
}
