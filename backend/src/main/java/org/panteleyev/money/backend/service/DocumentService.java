/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.DocumentRepository;
import org.panteleyev.money.model.MoneyDocument;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.objectMapper;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class DocumentService {
    private final DocumentRepository repository;
    private final Cache cache;

    public DocumentService(DocumentRepository repository, Cache documentCache) {
        this.repository = repository;
        this.cache = documentCache;
    }

    public Optional<MoneyDocument> get(UUID uuid) {
        return ServiceUtil.get(repository, cache, uuid);
    }

    public List<MoneyDocument> getAll() {
        return repository.getAll();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.getStream()) {
            writeStreamAsJsonArray(objectMapper, stream, out);
        }
    }

    public Optional<MoneyDocument> put(MoneyDocument document) {
        return ServiceUtil.put(repository, cache, document);
    }
}
