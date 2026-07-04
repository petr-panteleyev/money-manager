// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.ExchangeSecuritySplitsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.backend.service.ExchangeSecuritySplitV1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class ExchangeSecuritySplitsV1ApiImpl implements ExchangeSecuritySplitsV1ApiDelegate {
    private final ExchangeSecuritySplitV1Service service;

    public ExchangeSecuritySplitsV1ApiImpl(ExchangeSecuritySplitV1Service service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<ExchangeSecuritySplitFlatDTO>> getExchangeSecuritySplits() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getExchangeSecuritySplitsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }

    @Override
    public ResponseEntity<ExchangeSecuritySplitFlatDTO> getExchangeSecuritySplitByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<ExchangeSecuritySplitFlatDTO> putExchangeSecuritySplit(
            ExchangeSecuritySplitFlatDTO dto)
    {
        return ResponseEntity.ok(service.put(dto));
    }
}
