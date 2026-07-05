// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.IconFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class IconConverter {
    public IconFlatDTO entityToFlatDto(IconEntity entity) {
        if (entity == null) {
            return null;
        }
        var dto = new IconFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setBytes(entity.getBytes());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public IconEntity dtoToEntity(IconFlatDTO dto) {
        if (dto == null) {
            return null;
        }
        return new IconEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setBytes(dto.getBytes())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
