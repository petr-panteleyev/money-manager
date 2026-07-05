// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.ContactConverter;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ContactService {
    private final ContactRepository repository;
    private final IconRepository iconRepository;
    private final ContactConverter converter;

    public ContactService(
            ContactRepository repository,
            IconRepository iconRepository,
            ContactConverter converter)
    {
        this.repository = repository;
        this.iconRepository = iconRepository;
        this.converter = converter;
    }

    public List<ContactFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    public Optional<ContactFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    @Transactional
    public ContactFlatDTO put(ContactFlatDTO contact) {
        var iconEntity = contact.getIconUuid() == null ?
                null : iconRepository.getReferenceById(contact.getIconUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(contact, iconEntity)));
    }
}
