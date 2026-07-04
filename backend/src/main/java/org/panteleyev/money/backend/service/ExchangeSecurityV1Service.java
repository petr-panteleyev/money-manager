// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.converter.ExchangeSecurityConverter;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.backend.repository.ExchangeSecurityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class ExchangeSecurityV1Service {
    private final ObjectMapper objectMapper;
    private final ExchangeSecurityRepository repository;
    private final ExchangeSecurityConverter converter;

    public ExchangeSecurityV1Service(
            ObjectMapper objectMapper,
            ExchangeSecurityRepository repository,
            ExchangeSecurityConverter converter)
    {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.converter = converter;
    }

    @Transactional(readOnly = true)
    public List<ExchangeSecurityFlatDTO> getAll() {
        return repository.findAll().stream()
                .map(converter::entityToFlatDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<ExchangeSecurityFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public ExchangeSecurityFlatDTO put(ExchangeSecurityFlatDTO dto) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(dto)));
    }
}
