// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.rest;

import org.panteleyev.money.backend.openapi.api.InvestmentDealsV1ApiDelegate;
import org.panteleyev.money.backend.openapi.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.backend.service.InvestmentDealV1Service;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.UUID;

@Service
public class InvestmentDealsV1ApiImpl implements InvestmentDealsV1ApiDelegate {
    private final InvestmentDealV1Service service;

    public InvestmentDealsV1ApiImpl(InvestmentDealV1Service service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<InvestmentDealFlatDTO>> getInvestmentDeals() {
        return ResponseEntity.ok(service.getAll());
    }

    @Override
    public ResponseEntity<StreamingResponseBody> getInvestmentDealsAsStream() {
        return ResponseEntity.accepted().body(service::streamAll);
    }

    @Override
    public ResponseEntity<InvestmentDealFlatDTO> getInvestmentDealByUuid(UUID uuid) {
        return ResponseEntity.of(service.get(uuid));
    }

    @Override
    public ResponseEntity<InvestmentDealFlatDTO> putInvestmentDeal(InvestmentDealFlatDTO dto) {
        return ResponseEntity.ok(service.put(dto));
    }
}
