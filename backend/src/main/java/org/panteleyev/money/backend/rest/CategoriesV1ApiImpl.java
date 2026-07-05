// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CategoriesV1ApiDelegate;
import org.panteleyev.money.backend.service.CategoryService;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriesV1ApiImpl implements CategoriesV1ApiDelegate {
    private final CategoryService service;

    public CategoriesV1ApiImpl(CategoryService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CategoryFlatDTO>> getCategories() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CategoryFlatDTO> getCategoryByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CategoryFlatDTO> putCategory(CategoryFlatDTO category) {
        return ResponseEntity.ok(service.put(category));
    }
}
