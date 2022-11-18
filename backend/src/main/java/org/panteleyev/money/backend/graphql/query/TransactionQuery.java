/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.service.TransactionService;
import org.panteleyev.money.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionQuery implements GraphQLQueryResolver {
    private final TransactionService service;

    public TransactionQuery(TransactionService service) {
        this.service = service;
    }

    public Transaction transaction(UUID uuid) {
        return service.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Transaction", uuid));
    }

    public List<Transaction> transactionsByYearAndMonth(int year, int month) {
        return service.getByYearAndMonth(year, month);
    }
}
