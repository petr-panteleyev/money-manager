/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.repository.CurrencyRepository;
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

import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeObjectToStream;

@Controller
@CrossOrigin
@RequestMapping(CURRENCY_ROOT)
public class CurrencyController {
    private final CurrencyRepository currencyRepository;
    private final ObjectMapper objectMapper;

    public CurrencyController(CurrencyRepository currencyRepository, ObjectMapper objectMapper) {
        this.currencyRepository = currencyRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(currencyRepository.getAll());
    }

    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Currency> getCurrency(@PathVariable("uuid") UUID uuid) {
        return currencyRepository.get(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Currency> putCurrency(@PathVariable UUID uuid, @RequestBody Currency currency) {
        if (!uuid.equals(currency.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = currencyRepository.insertOrUpdate(currency);
        return rows == 1 ? ResponseEntity.ok(currency) : ResponseEntity.internalServerError().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = currencyRepository.getStream()) {
                stream.forEach(t -> writeObjectToStream(out, objectMapper, t));
            } finally {
                out.flush();
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
