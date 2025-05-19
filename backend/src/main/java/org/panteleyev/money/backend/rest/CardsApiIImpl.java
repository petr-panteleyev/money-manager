/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CardsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.CardFlatDto;
import org.panteleyev.money.backend.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class CardsApiIImpl implements CardsApiDelegate {
    private final CardService service;

    public CardsApiIImpl(CardService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CardFlatDto>> getCards() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CardFlatDto> getCardByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CardFlatDto> putCard(CardFlatDto category) {
        return ResponseEntity.ok(service.put(category));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getCardsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
