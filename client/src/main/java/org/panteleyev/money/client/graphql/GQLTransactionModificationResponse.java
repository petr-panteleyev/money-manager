/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.graphql;

import org.panteleyev.money.client.GraphQLError;
import org.panteleyev.money.client.dto.TransactionModificationResponseDto;

import java.util.List;
import java.util.Map;

public record GQLTransactionModificationResponse(
        Map<String, TransactionModificationResponseDto> data,
        List<GraphQLError> errors
) implements GQLScalarResponse<TransactionModificationResponseDto> {
}
