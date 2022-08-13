/*
 Copyright © 2021 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.model.MoneyRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoneyRepository<T extends MoneyRecord> {
    List<? extends MoneyRecord> getAll();

    Optional<T> get(UUID uuid);

    int insert(T record);

    int update(T record);
}
