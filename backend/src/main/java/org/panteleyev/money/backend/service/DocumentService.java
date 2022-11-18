/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.DocumentRepository;
import org.panteleyev.money.model.MoneyDocument;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

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

    public Optional<MoneyDocument> put(MoneyDocument document) {
        return ServiceUtil.put(repository, cache, document);
    }
}
