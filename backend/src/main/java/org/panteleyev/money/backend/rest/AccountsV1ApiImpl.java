// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.AccountsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDTO;
import org.panteleyev.money.backend.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class AccountsV1ApiImpl implements AccountsV1ApiDelegate {
    private final AccountService service;

    public AccountsV1ApiImpl(AccountService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<AccountFlatDTO>> getAccounts() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<AccountFlatDTO> getAccountByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<AccountFlatDTO> putAccount(AccountFlatDTO account) {
        return ResponseEntity.ok(service.put(account));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getAccountsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
