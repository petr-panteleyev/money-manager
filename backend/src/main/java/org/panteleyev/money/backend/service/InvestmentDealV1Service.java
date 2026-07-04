// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.converter.InvestmentDealConverter;
import org.panteleyev.money.backend.openapi.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.backend.repository.ExchangeSecurityRepository;
import org.panteleyev.money.backend.repository.InvestmentDealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class InvestmentDealV1Service {
    private final ObjectMapper objectMapper;
    private final InvestmentDealRepository repository;
    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeSecurityRepository exchangeSecurityRepository;
    private final InvestmentDealConverter converter;

    public InvestmentDealV1Service(
            ObjectMapper objectMapper,
            InvestmentDealRepository repository,
            AccountRepository accountRepository,
            CurrencyRepository currencyRepository,
            ExchangeSecurityRepository exchangeSecurityRepository,
            InvestmentDealConverter converter)
    {
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeSecurityRepository = exchangeSecurityRepository;
        this.converter = converter;
    }

    public List<InvestmentDealFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = repository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public Optional<InvestmentDealFlatDTO> get(UUID uuid) {
        return repository.findById(uuid).map(converter::entityToFlatDto);
    }

    public InvestmentDealFlatDTO put(InvestmentDealFlatDTO dto) {
        var account = accountRepository.getReferenceById(dto.getAccountUuid());
        var security = dto.getSecurityUuid() == null ?
                null : exchangeSecurityRepository.getReferenceById(dto.getSecurityUuid());
        var currency = dto.getCurrencyUuid() == null ?
                null : currencyRepository.getReferenceById(dto.getCurrencyUuid());

        var entity = converter.dtoToEntity(dto, account, security, currency);
        return converter.entityToFlatDto(repository.save(entity));
    }
}
