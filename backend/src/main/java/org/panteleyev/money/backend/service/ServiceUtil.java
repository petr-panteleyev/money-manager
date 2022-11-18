/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.MoneyRepository;
import org.panteleyev.money.model.MoneyRecord;
import org.springframework.cache.Cache;

import java.util.Optional;
import java.util.UUID;

final class ServiceUtil {
    private ServiceUtil() {
    }

    static <T extends MoneyRecord> Optional<T> get(MoneyRepository<T> repository, Cache cache, UUID uuid) {
        return Optional.ofNullable(cache.get(uuid, () -> repository.get(uuid).orElse(null)));
    }

    static <T extends MoneyRecord> Optional<T> put(MoneyRepository<T> repository, Cache cache, T record) {
        int rows = repository.insertOrUpdate(record);
        if (rows != 1) {
            return Optional.empty();
        } else {
            cache.put(record.uuid(), record);
            return Optional.of(record);
        }
    }

    static <T extends MoneyRecord> Optional<T> put(MoneyRepository<T> repository, T record) {
        int rows = repository.insertOrUpdate(record);
        if (rows != 1) {
            return Optional.empty();
        } else {
            return Optional.of(record);
        }
    }
}
