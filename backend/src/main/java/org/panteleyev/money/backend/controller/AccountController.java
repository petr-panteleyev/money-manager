/*
 Copyright © 2021-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

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

import java.util.List;
import java.util.UUID;

import static org.panteleyev.money.backend.WebmoneyApplication.ACCOUNT_ROOT;

@Controller
@CrossOrigin
@RequestMapping(ACCOUNT_ROOT)
public class AccountController {
    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
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

        var rows = 0;
        if (accountRepository.get(uuid).isEmpty()) {
            rows = accountRepository.insert(account);
        } else {
            rows = accountRepository.update(account);
        }
        return rows == 1 ? ResponseEntity.ok(account) : ResponseEntity.internalServerError().build();
    }
}
