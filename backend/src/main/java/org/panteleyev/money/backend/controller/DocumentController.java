/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.panteleyev.money.backend.repository.DocumentRepository;
import org.panteleyev.money.model.MoneyDocument;
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

import static org.panteleyev.money.backend.WebmoneyApplication.DOCUMENT_ROOT;
import static org.panteleyev.money.backend.controller.JsonUtil.writeStreamAsJsonArray;

@Tag(name = "Documents")
@Controller
@RequestMapping(DOCUMENT_ROOT)
@CrossOrigin
public class DocumentController {
    private final DocumentRepository documentRepository;
    private final ObjectMapper objectMapper;

    public DocumentController(DocumentRepository documentRepository, ObjectMapper objectMapper) {
        this.documentRepository = documentRepository;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Get all documents")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MoneyDocument>> getCurrencies() {
        return ResponseEntity.ok(documentRepository.getAll());
    }

    @Operation(summary = "Get document")
    @GetMapping(
            value = "/{uuid}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MoneyDocument> getDocument(@PathVariable("uuid") UUID uuid) {
        return ResponseEntity.of(documentRepository.get(uuid));
    }

    @Operation(summary = "Insert or update document")
    @PutMapping(
            value = "/{uuid}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MoneyDocument> putDocument(@PathVariable UUID uuid, @RequestBody MoneyDocument document) {
        if (!uuid.equals(document.uuid())) {
            return ResponseEntity.badRequest().build();
        }

        var rows = documentRepository.insertOrUpdate(document);
        return rows == 1 ? ResponseEntity.ok(document) : ResponseEntity.internalServerError().build();
    }

    @Operation(summary = "Get all documents as stream")
    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> getDocumentStream() {
        StreamingResponseBody body = (OutputStream out) -> {
            try (var stream = documentRepository.getStream()) {
                writeStreamAsJsonArray(objectMapper, stream, out);
            }
        };
        return ResponseEntity.accepted().body(body);
    }
}
