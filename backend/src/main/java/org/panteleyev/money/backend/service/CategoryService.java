// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.CategoryConverter;
import org.panteleyev.money.backend.repository.CategoryRepository;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final IconRepository iconRepository;
    private final CategoryConverter converter;

    public CategoryService(
            CategoryRepository repository,
            IconRepository iconRepository,
            CategoryConverter converter)
    {
        this.repository = repository;
        this.iconRepository = iconRepository;
        this.converter = converter;
    }

    public List<CategoryFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    public Optional<CategoryFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public CategoryFlatDTO put(CategoryFlatDTO category) {
        var iconEntity = category.getIconUuid() == null ?
                null : iconRepository.getReferenceById(category.getIconUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(category, iconEntity)));
    }
}
