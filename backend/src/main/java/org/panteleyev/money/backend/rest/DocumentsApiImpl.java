/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.DocumentsApiDelegate;
import org.panteleyev.money.backend.openapi.dto.DocumentFlatDto;
import org.panteleyev.money.backend.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentsApiImpl implements DocumentsApiDelegate {
    private final DocumentService service;

    public DocumentsApiImpl(DocumentService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<DocumentFlatDto>> getDocuments() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<DocumentFlatDto> getDocumentByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<DocumentFlatDto> putDocument(DocumentFlatDto document) {
        return ResponseEntity.ok(service.put(document));
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getDocumentsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }
}
