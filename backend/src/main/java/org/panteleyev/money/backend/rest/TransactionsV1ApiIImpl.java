// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.TransactionsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDTO;
import org.panteleyev.money.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionsV1ApiIImpl implements TransactionsV1ApiDelegate {
    private final TransactionService service;

    public TransactionsV1ApiIImpl(TransactionService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<TransactionFlatDTO>> getTransactions() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<TransactionFlatDTO> getTransactionByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<TransactionFlatDTO> putTransaction(TransactionFlatDTO transaction) {
        return ResponseEntity.ok(service.put(transaction));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getTransactionsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
