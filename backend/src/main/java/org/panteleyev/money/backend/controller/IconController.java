/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import org.panteleyev.money.backend.repository.IconRepository;
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

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.ICON_ROOT;

@Controller
@RequestMapping(ICON_ROOT)
@CrossOrigin
public class IconController {
    private final IconRepository iconRepository;

    public IconController(IconRepository iconRepository) {
        this.iconRepository = iconRepository;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Icon>> getIcons() {
        return ResponseEntity.ok(iconRepository.getAll());
    }

    @GetMapping(
        value = "/{uuid}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Icon> getIcon(@PathVariable UUID uuid) {
        return ResponseEntity.of(iconRepository.get(uuid));
    }

    @PutMapping(
        value = "/{uuid}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Icon> updateIcon(@PathVariable UUID uuid, @RequestBody Icon icon) {
        if (!uuid.equals(icon.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = 0;
        if (iconRepository.get(icon.uuid()).isEmpty()) {
            rows = iconRepository.insert(icon);
        } else {
            rows = iconRepository.update(icon);
        }
        return rows == 1 ? ResponseEntity.ok(icon) : ResponseEntity.internalServerError().build();
    }

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
}
