/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    @Query("SELECT a FROM Account a")
    Stream<AccountEntity> streamAll();

    @Query(nativeQuery = true, value = "SELECT COUNT(uuid) FROM account WHERE enabled=true")
    long getEnabledCount();
}
