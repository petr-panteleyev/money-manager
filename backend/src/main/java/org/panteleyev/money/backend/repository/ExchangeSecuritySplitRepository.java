// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.ExchangeSecuritySplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface ExchangeSecuritySplitRepository extends JpaRepository<ExchangeSecuritySplitEntity, UUID> {
    @Query("SELECT s FROM ExchangeSecuritySplit s")
    Stream<ExchangeSecuritySplitEntity> streamAll();
}
