// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDTO;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class ContactService {
    private final ObjectMapper objectMapper;
    private final ContactRepository repository;
    private final IconRepository iconRepository;
    private final EntityToDtoConverter converter;

    public ContactService(
            ObjectMapper objectMapper,
            ContactRepository repository,
            IconRepository iconRepository,
            EntityToDtoConverter converter)
    {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.iconRepository = iconRepository;
        this.converter = converter;
    }

    public List<ContactFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
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
