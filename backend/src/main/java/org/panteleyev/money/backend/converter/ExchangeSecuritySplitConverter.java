// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.ExchangeSecuritySplitEntity;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecuritySplitFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class ExchangeSecuritySplitConverter {

    public ExchangeSecuritySplitFlatDTO entityToFlatDto(ExchangeSecuritySplitEntity entity) {
        if (entity == null) return null;

        return new ExchangeSecuritySplitFlatDTO()
                .uuid(entity.getUuid())
                .securityUuid(entity.getSecurityUuid())
                .splitType(entity.getSplitType())
                .splitDate(entity.getSplitDate())
                .rate(entity.getRate())
                .comment(entity.getComment())
                .created(entity.getCreated())
                .modified(entity.getModified());
    }

    public ExchangeSecuritySplitEntity dtoToEntity(ExchangeSecuritySplitFlatDTO dto) {
        if (dto == null) return null;

        return new ExchangeSecuritySplitEntity()
                .setUuid(dto.getUuid())
                .setSecurityUuid(dto.getSecurityUuid())
                .setSplitType(dto.getSplitType())
                .setSplitDate(dto.getSplitDate())
                .setRate(dto.getRate())
                .setComment(dto.getComment())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
