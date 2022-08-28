/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.model.Account;
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

import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeObjectToStream;

@Controller
@CrossOrigin
@RequestMapping(ACCOUNT_ROOT)
public class AccountController {
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public AccountController(AccountRepository accountRepository, ObjectMapper objectMapper) {
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(accountRepository.getAll());
    }

    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Account> getAccount(@PathVariable("uuid") UUID uuid) {
        return accountRepository.get(uuid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Account> putAccount(@PathVariable UUID uuid, @RequestBody Account account) {
        if (!uuid.equals(account.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = accountRepository.insertOrUpdate(account);
        return rows == 1 ? ResponseEntity.ok(account) : ResponseEntity.internalServerError().build();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = accountRepository.getStream()) {
                stream.forEach(t -> writeObjectToStream(out, objectMapper, t));
            } finally {
                out.flush();
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
