/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.DocumentFlatDto;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.DocumentRepository;
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
    private final ContactRepository contactRepository;
    private final EntityToDtoConverter converter;

    public DocumentService(
            DocumentRepository repository,
            ContactRepository contactRepository,
            EntityToDtoConverter converter)
    {
        this.repository = repository;
        this.contactRepository = contactRepository;
        this.converter = converter;
    }

    public Optional<DocumentFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public List<DocumentFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    @Transactional
    public DocumentFlatDto put(DocumentFlatDto document) {
        var contact = contactRepository.getReferenceById(document.getContactUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(document, contact)));
    }
}
