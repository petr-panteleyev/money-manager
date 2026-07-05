// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.CurrencyConverter;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CurrencyService {
    private final CurrencyRepository repository;
    private final CurrencyConverter converter;

    public CurrencyService(
            CurrencyRepository repository,
            CurrencyConverter converter)
    {
        this.repository = repository;
        this.converter = converter;
    }

    public List<CurrencyFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto)
                .toList();
    }

    public Optional<CurrencyFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public CurrencyFlatDTO put(CurrencyFlatDTO currency) {
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(currency)));
    }
}
