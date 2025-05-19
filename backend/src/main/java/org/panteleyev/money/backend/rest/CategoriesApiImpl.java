/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CategoriesApiDelegate;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDto;
import org.panteleyev.money.backend.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class CategoriesApiImpl implements CategoriesApiDelegate {
    private final CategoryService service;

    public CategoriesApiImpl(CategoryService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CategoryFlatDto>> getCategories() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CategoryFlatDto> getCategoryByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CategoryFlatDto> putCategory(CategoryFlatDto category) {
        return ResponseEntity.ok(service.put(category));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getCategoriesAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
