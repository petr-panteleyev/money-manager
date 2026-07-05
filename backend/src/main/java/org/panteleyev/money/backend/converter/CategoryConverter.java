// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {

    public CategoryFlatDTO entityToFlatDto(CategoryEntity entity) {
        if (entity == null) return null;

        var dto = new CategoryFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setComment(entity.getComment());
        dto.setType(entity.getType());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CategoryEntity dtoToEntity(CategoryFlatDTO dto, IconEntity icon) {
        if (dto == null) return null;

        return new CategoryEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setComment(dto.getComment())
                .setType(dto.getType())
                .setIcon(icon)
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
