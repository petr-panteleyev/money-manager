// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.CardConverter;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CardRepository;
import org.panteleyev.money.dto.CardFlatDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardService {
    private final CardRepository repository;
    private final AccountRepository accountRepository;
    private final CardConverter converter;

    public CardService(
            CardRepository repository,
            AccountRepository accountRepository,
            CardConverter converter)
    {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.converter = converter;
    }

    public List<CardFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    public Optional<CardFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    @Transactional
    public CardFlatDTO put(CardFlatDTO card) {
        var account = accountRepository.getReferenceById(card.getAccountUuid());
        return converter.entityToFlatDto(repository.save(converter.dtoToEntity(card, account)));
    }
}
