/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import java.util.List;
import java.util.Map;

public record GraphQLError(
        String message,
        List<GraphQLSourceLocation> locations,
        List<String> path,
        Map<String, Object> extensions
) {
}
