// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class CurrencyConverter {
    public CurrencyFlatDTO entityToFlatDto(CurrencyEntity entity) {
        if (entity == null) return null;

        var dto = new CurrencyFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setSymbol(entity.getSymbol());
        dto.setDescription(entity.getDescription());
        dto.setFormatSymbol(entity.getFormatSymbol());
        dto.setFormatSymbolPosition(entity.getFormatSymbolPosition());
        dto.setShowFormatSymbol(entity.isShowFormatSymbol());
        dto.setDef(entity.isDef());
        dto.setRate(entity.getRate());
        dto.setDirection(entity.getDirection());
        dto.setUseThousandSeparator(entity.isUseThousandSeparator());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CurrencyEntity dtoToEntity(CurrencyFlatDTO dto) {
        if (dto == null) return null;

        return new CurrencyEntity()
                .setUuid(dto.getUuid())
                .setSymbol(dto.getSymbol())
                .setDescription(dto.getDescription())
                .setFormatSymbol(dto.getFormatSymbol())
                .setFormatSymbolPosition(dto.getFormatSymbolPosition())
                .setShowFormatSymbol(dto.getShowFormatSymbol())
                .setDef(dto.getDef())
                .setRate(dto.getRate())
                .setDirection(dto.getDirection())
                .setUseThousandSeparator(dto.getUseThousandSeparator())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
