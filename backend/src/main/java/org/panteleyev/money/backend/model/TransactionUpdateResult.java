// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.model;

import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.TransactionFlatDTO;

public record TransactionUpdateResult(
        TransactionFlatDTO transaction,
        ContactFlatDTO contact
) {
}
