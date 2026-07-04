// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface ExchangeSecurityRepository extends JpaRepository<ExchangeSecurityEntity, UUID> {
    @Query("SELECT s FROM ExchangeSecurity s")
    Stream<ExchangeSecurityEntity> streamAll();
}
