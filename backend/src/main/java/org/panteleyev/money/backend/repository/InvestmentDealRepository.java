// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.InvestmentDealEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface InvestmentDealRepository extends JpaRepository<InvestmentDealEntity, UUID> {
    @Query("SELECT i FROM InvestmentDeal i")
    Stream<InvestmentDealEntity> streamAll();
}
