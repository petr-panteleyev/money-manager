/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.objectMapper;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class ContactService {
    private final ContactRepository repository;
    private final IconRepository iconRepository;
    private final EntityToDtoConverter converter;

    public ContactService(ContactRepository repository, IconRepository iconRepository,
            EntityToDtoConverter converter)
    {
        this.repository = repository;
        this.iconRepository = iconRepository;
        this.converter = converter;
    }

    public List<ContactFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<ContactFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    @Transactional
    public ContactFlatDto put(ContactFlatDto contact) {
        var iconEntity = contact.getIconUuid() == null ?
                null : iconRepository.getReferenceById(contact.getIconUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(contact, iconEntity)));
    }
}
