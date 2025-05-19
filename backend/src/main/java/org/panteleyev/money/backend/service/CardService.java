/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.openapi.dto.CardFlatDto;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.objectMapper;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class CardService {
    private final CardRepository repository;
    private final AccountRepository accountRepository;
    private final EntityToDtoConverter converter;

    public CardService(CardRepository repository, AccountRepository accountRepository, EntityToDtoConverter converter) {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.converter = converter;
    }

    public List<CardFlatDto> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<CardFlatDto> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    @Transactional
    public CardFlatDto put(CardFlatDto card) {
        var account = accountRepository.getReferenceById(card.getAccountUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(card, account)));
    }
}
