// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.domain.InvestmentDealEntity;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class InvestmentDealConverter {
    public InvestmentDealFlatDTO entityToFlatDto(InvestmentDealEntity entity) {
        if (entity == null) return null;

        return new InvestmentDealFlatDTO()
                .uuid(entity.getUuid())
                .accountUuid(entity.getAccount().getUuid())
                .securityUuid(entity.getSecurity() == null ? null : entity.getSecurity().getUuid())
                .currencyUuid(entity.getCurrency() == null ? null : entity.getCurrency().getUuid())
                .dealNumber(entity.getDealNumber())
                .dealDate(entity.getDealDate())
                .accountingDate(entity.getAccountingDate())
                .marketType(entity.getMarketType())
                .operationType(entity.getOperationType())
                .securityAmount(entity.getSecurityAmount())
                .price(entity.getPrice())
                .aci(entity.getAci())
                .dealVolume(entity.getDealVolume())
                .rate(entity.getRate())
                .exchangeFee(entity.getExchangeFee())
                .brokerFee(entity.getBrokerFee())
                .amount(entity.getAmount())
                .dealType(entity.getDealType())
                .created(entity.getCreated())
                .modified(entity.getModified());
    }

    public InvestmentDealEntity dtoToEntity(
            InvestmentDealFlatDTO dto,
            AccountEntity account,
            ExchangeSecurityEntity security,
            CurrencyEntity currency)
    {
        if (dto == null) return null;

        return new InvestmentDealEntity()
                .setUuid(dto.getUuid())
                .setAccount(account)
                .setSecurity(security)
                .setCurrency(currency)
                .setDealNumber(dto.getDealNumber())
                .setDealDate(dto.getDealDate())
                .setAccountingDate(dto.getAccountingDate())
                .setMarketType(dto.getMarketType())
                .setOperationType(dto.getOperationType())
                .setSecurityAmount(dto.getSecurityAmount())
                .setPrice(dto.getPrice())
                .setAci(dto.getAci())
                .setDealVolume(dto.getDealVolume())
                .setRate(dto.getRate())
                .setExchangeFee(dto.getExchangeFee())
                .setBrokerFee(dto.getBrokerFee())
                .setAmount(dto.getAmount())
                .setDealType(dto.getDealType())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
