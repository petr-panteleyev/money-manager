// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.ExchangeSecuritiesV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.backend.service.ExchangeSecurityV1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class ExchangeSecuritiesV1ApiImpl implements ExchangeSecuritiesV1ApiDelegate {
    private final ExchangeSecurityV1Service service;

    public ExchangeSecuritiesV1ApiImpl(ExchangeSecurityV1Service service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<ExchangeSecurityFlatDTO>> getExchangeSecurities() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getExchangeSecuritiesAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }

    @Override
    public ResponseEntity<ExchangeSecurityFlatDTO> getExchangeSecurityByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<ExchangeSecurityFlatDTO> putExchangeSecurity(
            ExchangeSecurityFlatDTO exchangeSecurityFlatDto)
    {
        return ResponseEntity.ok(service.put(exchangeSecurityFlatDto));
    }
}
