// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.IconConverter;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.dto.IconFlatDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class IconService {
    private final IconRepository repository;
    private final IconConverter converter;

    public IconService(
            IconRepository repository,
            IconConverter converter)
    {
        this.repository = repository;
        this.converter = converter;
    }

    public List<IconFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    public Optional<IconFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public IconFlatDTO put(IconFlatDTO icon) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(icon)));
    }
}
