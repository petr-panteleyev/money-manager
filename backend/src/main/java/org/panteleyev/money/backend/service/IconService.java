/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.IconFlatDto;
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
public class IconService {
    private final IconRepository repository;
    private final EntityToDtoConverter converter;

    public IconService(IconRepository repository, EntityToDtoConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    public List<IconFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<IconFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public IconFlatDto put(IconFlatDto icon) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(icon)));
    }
}
