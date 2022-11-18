/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionResolver implements GraphQLResolver<Transaction> {
    private final AccountService accountService;
    private final ContactService contactService;
    private final TransactionRepository transactionRepository;

    public TransactionResolver(
            AccountService accountService,
            ContactService contactService,
            TransactionRepository transactionRepository
    ) {
        this.accountService = accountService;
        this.contactService = contactService;
        this.transactionRepository = transactionRepository;
    }

    public Account accountDebited(Transaction transaction) {
        return accountService.get(transaction.accountDebitedUuid()).orElseThrow();
    }

    public Account accountCredited(Transaction transaction) {
        return accountService.get(transaction.accountCreditedUuid()).orElseThrow();
    }

    public Contact contact(Transaction transaction) {
        return transaction.contactUuid() == null ?
                null : contactService.get(transaction.contactUuid()).orElseThrow();
    }

    public Transaction parent(Transaction transaction) {
        return transaction.parentUuid() == null ?
                null : transactionRepository.get(transaction.parentUuid()).orElseThrow();
    }
}
