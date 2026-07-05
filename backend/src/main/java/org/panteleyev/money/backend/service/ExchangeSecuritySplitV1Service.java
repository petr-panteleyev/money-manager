// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.ExchangeSecuritySplitConverter;
import org.panteleyev.money.backend.repository.ExchangeSecuritySplitRepository;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExchangeSecuritySplitV1Service {
    private final ExchangeSecuritySplitRepository repository;
    private final ExchangeSecuritySplitConverter converter;

    public ExchangeSecuritySplitV1Service(
            ExchangeSecuritySplitRepository repository,
            ExchangeSecuritySplitConverter converter)
    {
        this.repository = repository;
        this.converter = converter;
    }

    public List<ExchangeSecuritySplitFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    public Optional<ExchangeSecuritySplitFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public ExchangeSecuritySplitFlatDTO put(ExchangeSecuritySplitFlatDTO dto) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(dto)));
    }
}
