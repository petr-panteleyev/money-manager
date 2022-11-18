/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import org.panteleyev.money.client.dto.MoneyDto;

import java.util.List;

public record GraphQLListResponse<T extends MoneyDto>(
        String operation,
        List<T> payload
) {
}
