/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.input;

import org.panteleyev.money.model.CategoryType;

import java.util.UUID;

public record CategoryInput(
        String name,
        CategoryType type,
        String comment,
        UUID iconUuid
) {
}
