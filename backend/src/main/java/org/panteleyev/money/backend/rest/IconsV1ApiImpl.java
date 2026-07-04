// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.IconsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;
import org.panteleyev.money.backend.service.IconService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class IconsV1ApiImpl implements IconsV1ApiDelegate {
    private final IconService service;

    public IconsV1ApiImpl(IconService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<IconFlatDTO>> getIcons() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<IconFlatDTO> getIconByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<IconFlatDTO> putIcon(IconFlatDTO icon) {
        return ResponseEntity.ok(service.put(icon));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getIconsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
