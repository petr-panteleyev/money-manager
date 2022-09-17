/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.repository.CategoryRepository;
import org.panteleyev.money.model.Category;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CATEGORY_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeStreamAsJsonArray;

@Controller
@CrossOrigin
@RequestMapping(CATEGORY_ROOT)
public class CategoryController {
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    public CategoryController(CategoryRepository categoryRepository, ObjectMapper objectMapper) {
        this.categoryRepository = categoryRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.getAll());
    }

    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Category> getCategory(@PathVariable UUID uuid) {
        return categoryRepository.get(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Category> putCategory(@PathVariable UUID uuid, @RequestBody Category category) {
        if (!uuid.equals(category.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = categoryRepository.insertOrUpdate(category);
        return rows == 1 ? ResponseEntity.ok(category) : ResponseEntity.internalServerError().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = categoryRepository.getStream()) {
                writeStreamAsJsonArray(objectMapper, stream, out);
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
