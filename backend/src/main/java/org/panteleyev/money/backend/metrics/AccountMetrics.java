/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.panteleyev.money.backend.service.AccountService;
import org.springframework.stereotype.Component;

@Component
public class AccountMetrics implements MeterBinder {
    private final AccountService accountService;

    public AccountMetrics(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        Gauge.builder("org.panteleyev.money.table.count", () -> accountService.getCount(true))
                .description("Total amount of accounts")
                .tags("table", "account", "status", "total")
                .register(meterRegistry);

        Gauge.builder("org.panteleyev.money.table.count", () -> accountService.getCount(false))
                .description("Total amount of active accounts")
                .tags("table", "account", "status", "active")
                .register(meterRegistry);

    }
}
