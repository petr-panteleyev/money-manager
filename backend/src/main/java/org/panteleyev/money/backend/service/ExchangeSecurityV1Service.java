// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.ExchangeSecurityConverter;
import org.panteleyev.money.backend.repository.ExchangeSecurityRepository;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExchangeSecurityV1Service {
    private final ExchangeSecurityRepository repository;
    private final ExchangeSecurityConverter converter;

    public ExchangeSecurityV1Service(
            ExchangeSecurityRepository repository,
            ExchangeSecurityConverter converter)
    {
        this.repository = repository;
        this.converter = converter;
    }

    @Transactional(readOnly = true)
    public List<ExchangeSecurityFlatDTO> getAll() {
        return repository.findAll().stream()
                .map(converter::entityToFlatDto)
                .toList();
    }

    public Optional<ExchangeSecurityFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public ExchangeSecurityFlatDTO put(ExchangeSecurityFlatDTO dto) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(dto)));
    }
}
