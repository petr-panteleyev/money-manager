/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.graphql;

import org.panteleyev.money.client.dto.MoneyDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GQLListResponse<T extends MoneyDto> {
    Map<String, List<T>> data();

    default Optional<String> getOperation() {
        return data().entrySet().stream()
                .findFirst()
                .map(Map.Entry::getKey);
    }

    default List<T> getPayload() {
        return data().entrySet().stream()
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(List.of());
    }
}
