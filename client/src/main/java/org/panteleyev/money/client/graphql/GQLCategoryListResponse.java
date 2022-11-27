/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.graphql;

import org.panteleyev.money.client.GraphQLError;
import org.panteleyev.money.client.dto.CategoryDto;

import java.util.List;
import java.util.Map;

public record GQLCategoryListResponse(
        Map<String, List<CategoryDto>> data,
        List<GraphQLError> errors
) implements GQLListResponse<CategoryDto> {
}
