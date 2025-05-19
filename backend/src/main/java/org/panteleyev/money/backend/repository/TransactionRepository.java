/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    @Query("SELECT t FROM Transaction t")
    Stream<TransactionEntity> streamAll();

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.transactionDate >= :dateFrom AND t.transactionDate <= :dateTo
            """)
    List<TransactionEntity> findByDateRange(LocalDate dateFrom, LocalDate dateTo);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.accountDebited.uuid = :accountUuid OR t.accountCredited.uuid = :accountUuid
            """)
    Stream<TransactionEntity> streamByAccountId(UUID accountUuid);
}
