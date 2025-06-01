/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;


@Configuration
public class CacheConfiguration {
    private static final String CACHE_ICONS = "cache_icons";
    private static final String CACHE_CATEGORIES = "cache_categories";
    private static final String CACHE_ACCOUNTS = "cache_accounts";
    private static final String CACHE_CURRENCIES = "cache_currencies";
    private static final String CACHE_CONTACTS = "cache_contacts";
    private static final String CACHE_DOCUMENTS = "cache_documents";

    @Bean
    public Cache iconCache() {
        return createCache(CACHE_ICONS);
    }

    @Bean
    public Cache categoryCache() {
        return createCache(CACHE_CATEGORIES);
    }

    @Bean
    public Cache accountCache() {
        return createCache(CACHE_ACCOUNTS);
    }

    @Bean
    public Cache currencyCache() {
        return createCache(CACHE_CURRENCIES);
    }

    @Bean
    public Cache contactCache() {
        return createCache(CACHE_CONTACTS);
    }

    @Bean
    public Cache documentCache() {
        return createCache(CACHE_DOCUMENTS);
    }

    private static Cache createCache(String name) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .initialCapacity(1000)
                .maximumSize(10000)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build());
    }
}
