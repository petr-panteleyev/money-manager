/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.service.AccountService;
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

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;

@Tag(name = "Accounts")
@Controller
@CrossOrigin
@RequestMapping(ACCOUNT_ROOT)
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @Operation(summary = "Get all accounts")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Account>> getAccounts() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Get account")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Account> getAccount(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Operation(summary = "Insert or update account")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Account> putAccount(@PathVariable UUID uuid, @RequestBody Account account) {
        if (!uuid.equals(account.uuid())) {
            return ResponseEntity.badRequest().build();
        } else {
            return service.put(account)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        }
    }

    @Operation(summary = "Get all accounts as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
