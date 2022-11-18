/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.graphql;

import org.panteleyev.money.client.dto.AccountDto;

import java.util.Map;

public record GQLAccountResponse(Map<String, AccountDto> data) implements GQLScalarResponse<AccountDto> {
}
