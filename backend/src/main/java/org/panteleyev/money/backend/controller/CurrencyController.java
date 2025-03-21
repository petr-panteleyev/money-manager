/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.service.CurrencyService;
import org.panteleyev.money.model.Currency;
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

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;

@Tag(name = "Currencies")
@Controller
@CrossOrigin
@RequestMapping(CURRENCY_ROOT)
public class CurrencyController {
    private final CurrencyService service;

    public CurrencyController(CurrencyService service) {
        this.service = service;
    }

    @Operation(summary = "Get all currencies")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Get currency")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Currency> getCurrency(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Operation(summary = "Insert or update currency")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Currency> putCurrency(@PathVariable UUID uuid, @RequestBody Currency currency) {
        if (!uuid.equals(currency.uuid())) {
            return ResponseEntity.badRequest().build();
        } else {
            return service.put(currency)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        }
    }

    @Operation(summary = "Get all currencies as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
