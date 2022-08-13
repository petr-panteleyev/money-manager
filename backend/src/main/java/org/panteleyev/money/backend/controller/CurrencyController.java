/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

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

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.CURRENCY_ROOT;

@Controller
@CrossOrigin
@RequestMapping(CURRENCY_ROOT)
public class CurrencyController {
    private final CurrencyRepository currencyRepository;

    public CurrencyController(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
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

        var rows = 0;
        if (currencyRepository.get(uuid).isEmpty()) {
            rows = currencyRepository.insert(currency);
        } else {
            rows = currencyRepository.update(currency);
        }
        return rows == 1 ? ResponseEntity.ok(currency) : ResponseEntity.internalServerError().build();
    }
}
