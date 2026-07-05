// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.model;

import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.TransactionFlatDTO;

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
        TransactionFlatDTO transaction,
        ContactFlatDTO contact,
        Collection<AccountFlatDTO> accounts
) {
}
