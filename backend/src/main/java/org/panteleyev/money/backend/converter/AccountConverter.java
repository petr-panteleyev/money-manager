// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.AccountFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter {
    public AccountFlatDTO entityToFlatDto(AccountEntity entity) {
        if (entity == null) return null;

        var dto = new AccountFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setComment(entity.getComment());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setOpeningBalance(entity.getOpeningBalance());
        dto.setAccountLimit(entity.getAccountLimit());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setType(entity.getType());
        dto.setCategoryUuid(entity.getCategory().getUuid());
        dto.setCurrencyUuid(entity.getCurrency() == null ? null : entity.getCurrency().getUuid());
        dto.setSecurityUuid(entity.getSecurity() == null ? null : entity.getSecurity().getUuid());
        dto.setEnabled(entity.isEnabled());
        dto.setInterest(entity.getInterest());
        dto.setClosingDate(entity.getClosingDate());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setTotal(entity.getTotal());
        dto.setTotalWaiting(entity.getTotalWaiting());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public AccountEntity dtoToEntity(AccountFlatDTO dto, CategoryEntity category, CurrencyEntity currency,
            ExchangeSecurityEntity security, IconEntity icon)
    {
        if (dto == null) return null;

        return new AccountEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setComment(dto.getComment())
                .setAccountNumber(dto.getAccountNumber())
                .setOpeningBalance(dto.getOpeningBalance())
                .setAccountLimit(dto.getAccountLimit())
                .setCurrencyRate(dto.getCurrencyRate())
                .setType(category.getType())
                .setCategory(category)
                .setCurrency(currency)
                .setSecurity(security)
                .setEnabled(dto.getEnabled())
                .setInterest(dto.getInterest())
                .setClosingDate(dto.getClosingDate())
                .setIcon(icon)
                .setTotal(dto.getTotal())
                .setTotalWaiting(dto.getTotalWaiting())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

}
