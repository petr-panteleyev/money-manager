/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.CategoryFlatDto;
import org.panteleyev.money.backend.repository.CategoryRepository;
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
public class CategoryService {
    private final CategoryRepository repository;
    private final IconRepository iconRepository;
    private final EntityToDtoConverter converter;

    public CategoryService(
            CategoryRepository repository,
            IconRepository iconRepository,
            EntityToDtoConverter converter)
    {
        this.repository = repository;
        this.iconRepository = iconRepository;
        this.converter = converter;
    }

    public List<CategoryFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<CategoryFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public CategoryFlatDto put(CategoryFlatDto category) {
        var iconEntity = category.getIconUuid() == null ?
                null : iconRepository.getReferenceById(category.getIconUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(category, iconEntity)));
    }
}
