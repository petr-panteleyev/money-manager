/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.TransactionsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDto;
import org.panteleyev.money.backend.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionsApiIImpl implements TransactionsApiDelegate {
    private final TransactionService service;

    public TransactionsApiIImpl(TransactionService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<TransactionFlatDto>> getTransactions() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<TransactionFlatDto> getTransactionByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<TransactionFlatDto> putTransaction(TransactionFlatDto transaction) {
        return ResponseEntity.ok(service.put(transaction));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getTransactionsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
