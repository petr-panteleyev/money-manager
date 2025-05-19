/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.IconsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.IconFlatDto;
import org.panteleyev.money.backend.service.IconService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class IconsApiImpl implements IconsApiDelegate {
    private final IconService service;

    public IconsApiImpl(IconService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<IconFlatDto>> getIcons() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<IconFlatDto> getIconByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<IconFlatDto> putIcon(IconFlatDto icon) {
        return ResponseEntity.ok(service.put(icon));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getIconsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
