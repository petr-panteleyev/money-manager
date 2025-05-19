/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.objectMapper;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class CurrencyService {
    private final CurrencyRepository repository;
    private final EntityToDtoConverter converter;

    public CurrencyService(CurrencyRepository repository, EntityToDtoConverter converter) {
        this.repository = repository;
        this.converter = converter;
    }

    public List<CurrencyFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<CurrencyFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public CurrencyFlatDto put(CurrencyFlatDto currency) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(currency)));
    }
}
