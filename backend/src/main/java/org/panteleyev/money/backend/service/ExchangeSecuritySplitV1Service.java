// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.converter.ExchangeSecuritySplitConverter;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.backend.repository.ExchangeSecuritySplitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class ExchangeSecuritySplitV1Service {
    private final ObjectMapper objectMapper;
    private final ExchangeSecuritySplitRepository repository;
    private final ExchangeSecuritySplitConverter converter;

    public ExchangeSecuritySplitV1Service(
            ObjectMapper objectMapper,
            ExchangeSecuritySplitRepository repository,
            ExchangeSecuritySplitConverter converter)
    {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.converter = converter;
    }

    public List<ExchangeSecuritySplitFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<ExchangeSecuritySplitFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public ExchangeSecuritySplitFlatDTO put(ExchangeSecuritySplitFlatDTO dto) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(dto)));
    }
}
