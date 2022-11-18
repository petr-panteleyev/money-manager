/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.model.Icon;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class IconService {
    private final IconRepository repository;
    private final Cache cache;

    public IconService(IconRepository repository, Cache iconCache) {
        this.repository = repository;
        this.cache = iconCache;
    }

    public Optional<Icon> get(UUID uuid) {
        return ServiceUtil.get(repository, cache, uuid);
    }

    public Optional<Icon> put(Icon icon) {
        return ServiceUtil.put(repository, cache, icon);
    }
}
