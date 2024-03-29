/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.repository.IconRepository;
import org.panteleyev.money.backend.service.IconService;
import org.panteleyev.money.model.Icon;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeStreamAsJsonArray;

@Tag(name = "Icons")
@Controller
@RequestMapping(ICON_ROOT)
@CrossOrigin
public class IconController {
    private final IconRepository iconRepository;
    private final IconService service;
    private final ObjectMapper objectMapper;

    public IconController(IconRepository iconRepository, IconService service, ObjectMapper objectMapper) {
        this.iconRepository = iconRepository;
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Get all icons")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Icon>> getIcons() {
        return ResponseEntity.ok(iconRepository.getAll());
    }

    @Operation(summary = "Get icon")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Icon> getIcon(@PathVariable UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Operation(summary = "Insert or update icon by id")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Icon> updateIcon(@PathVariable UUID uuid, @RequestBody Icon icon) {
        if (!uuid.equals(icon.uuid())) {
            return ResponseEntity.badRequest().build();
        } else {
            return service.put(icon)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        }
    }

    @Operation(summary = "Get icon image bytes")
    @GetMapping(
            value = "/{uuid}/bytes",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    ResponseEntity<byte[]> getIconBytes(@PathVariable UUID uuid) {
        return ResponseEntity.of(
                iconRepository.get(uuid)
                        .map(Icon::bytes)
        );
    }

    @Operation(summary = "Get all icons as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = iconRepository.getStream()) {
                writeStreamAsJsonArray(objectMapper, stream, out);
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
