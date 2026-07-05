// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CardsV1ApiDelegate;
import org.panteleyev.money.backend.service.CardService;
import org.panteleyev.money.dto.CardFlatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CardsV1ApiImpl implements CardsV1ApiDelegate {
    private final CardService service;

    public CardsV1ApiImpl(CardService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CardFlatDTO>> getCards() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CardFlatDTO> getCardByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CardFlatDTO> putCard(CardFlatDTO category) {
        return ResponseEntity.ok(service.put(category));
    }
}
