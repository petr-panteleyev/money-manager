/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.model.Contact;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContactService {
    private final ContactRepository repository;
    private final Cache cache;

    public ContactService(ContactRepository repository, Cache contactCache) {
        this.repository = repository;
        this.cache = contactCache;
    }

    public List<Contact> getAll() {
        return repository.getAll();
    }

    public Optional<Contact> get(UUID uuid) {
        return ServiceUtil.get(repository, cache, uuid);
    }

    public Optional<Contact> put(Contact contact) {
        return ServiceUtil.put(repository, cache, contact);
    }
}
