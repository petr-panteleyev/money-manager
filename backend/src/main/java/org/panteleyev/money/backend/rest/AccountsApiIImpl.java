/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.AccountsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDto;
import org.panteleyev.money.backend.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class AccountsApiIImpl implements AccountsApiDelegate {
    private final AccountService service;

    public AccountsApiIImpl(AccountService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<AccountFlatDto>> getAccounts() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<AccountFlatDto> getAccountByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<AccountFlatDto> putAccount(AccountFlatDto account) {
        return ResponseEntity.ok(service.put(account));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getAccountsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
