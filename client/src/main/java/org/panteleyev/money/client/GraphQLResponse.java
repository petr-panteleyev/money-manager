/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import org.panteleyev.money.client.dto.MoneyDto;

public record GraphQLResponse<T extends MoneyDto>(
        String operation,
        T payload
) {
}
