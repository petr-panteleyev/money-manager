/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.CategoryRepository;
import org.panteleyev.money.model.Category;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final Cache cache;

    public CategoryService(CategoryRepository repository, Cache categoryCache) {
        this.repository = repository;
        this.cache = categoryCache;
    }

    public List<Category> getAll() {
        return repository.getAll();
    }

    public Optional<Category> get(UUID uuid) {
        return ServiceUtil.get(repository, cache, uuid);
    }

    public Optional<Category> put(Category category) {
        return ServiceUtil.put(repository, cache, category);
    }
}
