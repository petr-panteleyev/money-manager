// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.converter.InvestmentDealConverter;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CurrencyRepository;
import org.panteleyev.money.backend.repository.ExchangeSecurityRepository;
import org.panteleyev.money.backend.repository.InvestmentDealRepository;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InvestmentDealV1Service {
    private final InvestmentDealRepository repository;
    private final AccountRepository accountRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeSecurityRepository exchangeSecurityRepository;
    private final InvestmentDealConverter converter;

    public InvestmentDealV1Service(
            InvestmentDealRepository repository,
            AccountRepository accountRepository,
            CurrencyRepository currencyRepository,
            ExchangeSecurityRepository exchangeSecurityRepository,
            InvestmentDealConverter converter)
    {
        this.repository = repository;
        this.accountRepository = accountRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeSecurityRepository = exchangeSecurityRepository;
        this.converter = converter;
    }

    public List<InvestmentDealFlatDTO> getAll() {
        return repository.findAll().stream().map(converter::entityToFlatDto).toList();
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
