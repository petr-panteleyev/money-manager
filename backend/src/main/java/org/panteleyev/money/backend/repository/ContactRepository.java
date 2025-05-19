/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.ContactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface ContactRepository extends JpaRepository<ContactEntity, UUID> {
    @Query("SELECT c FROM Contact c")
    Stream<ContactEntity> streamAll();
}
