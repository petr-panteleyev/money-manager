/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.graphql;

import org.panteleyev.money.client.GraphQLError;
import org.panteleyev.money.client.dto.ContactDto;

import java.util.List;
import java.util.Map;

public record GQLContactListResponse(
        Map<String, List<ContactDto>> data,
        List<GraphQLError> errors
) implements GQLListResponse<ContactDto> {
}
