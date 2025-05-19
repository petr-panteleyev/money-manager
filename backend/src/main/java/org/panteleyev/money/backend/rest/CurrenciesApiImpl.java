/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.CurrenciesApiDelegate;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.backend.service.CurrencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class CurrenciesApiImpl implements CurrenciesApiDelegate {
    private final CurrencyService service;

    public CurrenciesApiImpl(CurrencyService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<CurrencyFlatDto>> getCurrencies() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<CurrencyFlatDto> getCurrencyByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<CurrencyFlatDto> putCurrency(CurrencyFlatDto currency) {
        return ResponseEntity.ok(service.put(currency));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getCurrenciesAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
