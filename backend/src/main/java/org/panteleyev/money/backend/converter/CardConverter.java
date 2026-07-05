// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.dto.CardFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class CardConverter {
    public CardFlatDTO entityToFlatDto(CardEntity entity) {
        if (entity == null) return null;

        var dto = new CardFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setAccountUuid(entity.getAccount() == null ? null : entity.getAccount().getUuid());
        dto.setType(entity.getType());
        dto.setNumber(entity.getNumber());
        dto.setExpiration(entity.getExpiration());
        dto.setComment(entity.getComment());
        dto.setEnabled(entity.isEnabled());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CardEntity dtoToEntity(CardFlatDTO dto, AccountEntity accountEntity) {
        if (dto == null) return null;

        return new CardEntity()
                .setUuid(dto.getUuid())
                .setAccount(accountEntity)
                .setType(dto.getType())
                .setNumber(dto.getNumber())
                .setExpiration(dto.getExpiration())
                .setComment(dto.getComment())
                .setEnabled(dto.getEnabled())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
