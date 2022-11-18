/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.model.Currency;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrencyService {
    private final CurrencyRepository repository;
    private final Cache cache;

    public CurrencyService(CurrencyRepository repository, Cache currencyCache) {
        this.repository = repository;
        this.cache = currencyCache;
    }

    public List<Currency> getAll() {
        return repository.getAll();
    }

    public Optional<Currency> get(UUID uuid) {
        return ServiceUtil.get(repository, cache, uuid);
    }

    public Optional<Currency> put(Currency currency) {
        return ServiceUtil.put(repository, cache, currency);
    }
}
