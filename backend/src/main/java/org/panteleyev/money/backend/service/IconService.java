// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;
import org.panteleyev.money.backend.repository.IconRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class IconService {
    private final ObjectMapper objectMapper;
    private final IconRepository repository;
    private final EntityToDtoConverter converter;

    public IconService(
            ObjectMapper objectMapper,
            IconRepository repository,
            EntityToDtoConverter converter)
    {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.converter = converter;
    }

    public List<IconFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<IconFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public IconFlatDTO put(IconFlatDTO icon) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(icon)));
    }
}
