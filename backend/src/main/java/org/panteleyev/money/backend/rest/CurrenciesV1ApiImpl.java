// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CurrenciesV1ApiDelegate;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CurrenciesV1ApiImpl implements CurrenciesV1ApiDelegate {
    private final CurrencyService service;

    public CurrenciesV1ApiImpl(CurrencyService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CurrencyFlatDTO>> getCurrencies() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CurrencyFlatDTO> getCurrencyByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CurrencyFlatDTO> putCurrency(CurrencyFlatDTO currency) {
        return ResponseEntity.ok(service.put(currency));
    }
}
