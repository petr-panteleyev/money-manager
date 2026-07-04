// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.ExchangeSecurityEntity;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class ExchangeSecurityConverter {
    public ExchangeSecurityFlatDTO entityToFlatDto(ExchangeSecurityEntity entity) {
        if (entity == null) return null;

        return new ExchangeSecurityFlatDTO()
                .uuid(entity.getUuid())
                .secId(entity.getSecId())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .isin(entity.getIsin())
                .regNumber(entity.getRegNumber())
                .faceValue(entity.getFaceValue())
                .issueDate(entity.getIssueDate())
                .matDate(entity.getMatDate())
                .daysToRedemption(entity.getDaysToRedemption())
                .groupType(entity.getGroupType())
                .groupName(entity.getGroupName())
                .type(entity.getType())
                .typeName(entity.getTypeName())
                .marketValue(entity.getMarketValue())
                .couponValue(entity.getCouponValue())
                .couponPercent(entity.getCouponPercent())
                .couponDate(entity.getCouponDate())
                .couponFrequency(entity.getCouponFrequency())
                .accruedInterest(entity.getAccruedInterest())
                .couponPeriod(entity.getCouponPeriod())
                .created(entity.getCreated())
                .modified(entity.getModified());
    }

    public ExchangeSecurityEntity dtoToEntity(ExchangeSecurityFlatDTO dto) {
        if (dto == null) return null;

        return new ExchangeSecurityEntity()
                .setUuid(dto.getUuid())
                .setSecId(dto.getSecId())
                .setName(dto.getName())
                .setShortName(dto.getShortName())
                .setIsin(dto.getIsin())
                .setRegNumber(dto.getRegNumber())
                .setFaceValue(dto.getFaceValue())
                .setIssueDate(dto.getIssueDate())
                .setMatDate(dto.getMatDate())
                .setDaysToRedemption(dto.getDaysToRedemption())
                .setGroupType(dto.getGroupType())
                .setGroupName(dto.getGroupName())
                .setType(dto.getType())
                .setTypeName(dto.getTypeName())
                .setMarketValue(dto.getMarketValue())
                .setCouponValue(dto.getCouponValue())
                .setCouponPercent(dto.getCouponPercent())
                .setCouponDate(dto.getCouponDate())
                .setCouponFrequency(dto.getCouponFrequency())
                .setAccruedInterest(dto.getAccruedInterest())
                .setCouponPeriod(dto.getCouponPeriod())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
