/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.service.TransactionService;
import org.panteleyev.money.model.Transaction;
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

import static org.panteleyev.money.backend.WebmoneyApplication.TRANSACTION_ROOT;

@Tag(name = "Transactions")
@Controller
@CrossOrigin
@RequestMapping(TRANSACTION_ROOT)
public class TransactionController {
    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @Operation(summary = "Get all transactions")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Transaction>> getTransactions() {
        return ResponseEntity.ok(service.getAll());
    }

    @Operation(summary = "Get transaction")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Transaction> getTransaction(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Operation(summary = "Insert or update transaction")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Transaction> putTransaction(@PathVariable UUID uuid, @RequestBody Transaction transaction) {
        if (!uuid.equals(transaction.uuid())) {
            return ResponseEntity.badRequest().build();
        } else {
            return service.put(transaction)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.internalServerError().build());
        }
    }

    @Operation(summary = "Get all transactions as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getTransactionStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
