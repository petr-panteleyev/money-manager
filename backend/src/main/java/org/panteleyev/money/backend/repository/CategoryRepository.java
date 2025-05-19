/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.repository;

import org.panteleyev.money.backend.domain.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;
import java.util.stream.Stream;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    @Query("SELECT c FROM Category c")
    Stream<CategoryEntity> streamAll();
}
