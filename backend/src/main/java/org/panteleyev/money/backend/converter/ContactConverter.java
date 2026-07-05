// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class ContactConverter {
    public ContactFlatDTO entityToFlatDto(ContactEntity entity) {
        if (entity == null) return null;

        var dto = new ContactFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setComment(entity.getComment());
        dto.setPhone(entity.getPhone());
        dto.setMobile(entity.getMobile());
        dto.setEmail(entity.getEmail());
        dto.setWeb(entity.getWeb());
        dto.setStreet(entity.getStreet());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setZip(entity.getZip());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public ContactEntity dtoToEntity(ContactFlatDTO dto, IconEntity icon) {
        if (dto == null) return null;

        return new ContactEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setType(dto.getType())
                .setComment(dto.getComment())
                .setPhone(dto.getPhone())
                .setMobile(dto.getMobile())
                .setEmail(dto.getEmail())
                .setWeb(dto.getWeb())
                .setStreet(dto.getStreet())
                .setCity(dto.getCity())
                .setCountry(dto.getCountry())
                .setZip(dto.getZip())
                .setIcon(icon)
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
