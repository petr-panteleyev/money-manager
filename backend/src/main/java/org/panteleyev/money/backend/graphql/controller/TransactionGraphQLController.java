/*
 Copyright © 2022-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.controller;

import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.TransactionInput;
import org.panteleyev.money.backend.model.TransactionOperationResult;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.backend.service.TransactionService;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Transaction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Controller
public class TransactionGraphQLController {
    private final AccountService accountService;
    private final ContactService contactService;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;

    public TransactionGraphQLController(
            AccountService accountService,
            ContactService contactService,
            TransactionRepository transactionRepository,
            TransactionService transactionService
    ) {
        this.accountService = accountService;
        this.contactService = contactService;
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
    }

    @QueryMapping
    public Transaction transaction(@Argument UUID uuid) {
        return transactionService.get(uuid)
                .orElseThrow(() -> new GraphQLNotFoundException("Transaction", uuid));
    }

    @QueryMapping
    public List<Transaction> transactionsByYearAndMonth(
            @Argument int year,
            @Argument int month
    ) {
        return transactionService.getByYearAndMonth(year, month);
    }

    @Transactional(rollbackFor = Exception.class)
    @MutationMapping
    public TransactionOperationResult createTransaction(@Argument TransactionInput input) {
        var debitedAccount = accountService.get(input.accountDebitedUuid()).orElseThrow();
        var creditedAccount = accountService.get(input.accountCreditedUuid()).orElseThrow();
        if (input.parentUuid() != null) {
            transactionService.get(input.parentUuid()).orElseThrow();
        }

        var builder = new Transaction.Builder()
                .amount(input.amount())
                .creditAmount(input.creditAmount())
                .transactionDate(input.transactionDate())
                .type(input.type())
                .comment(input.comment())
                .checked(input.checked())
                .accountDebitedUuid(debitedAccount.uuid())
                .accountCreditedUuid(creditedAccount.uuid())
                .accountDebitedType(debitedAccount.type())
                .accountCreditedType(creditedAccount.type())
                .accountDebitedCategoryUuid(debitedAccount.categoryUuid())
                .accountCreditedCategoryUuid(creditedAccount.categoryUuid())
                .invoiceNumber(input.invoiceNumber())
                .parentUuid(input.parentUuid())
                .detailed(input.detailed())
                .statementDate(input.statementDate());

        var contact = setupContact(null, builder, input);

        var createResult = transactionService.createOrUpdateTransaction(
                builder.build(),
                contact
        ).orElseThrow(() -> new GraphQLCreateException("Transaction"));

        var accountIds = new HashSet<UUID>();
        accountIds.add(debitedAccount.uuid());
        accountIds.add(creditedAccount.uuid());
        var accounts = accountService.updateBalances(accountIds);

        return new TransactionOperationResult(
                createResult.transaction(),
                createResult.contact(),
                accounts
        );
    }

    @Transactional(rollbackFor = Exception.class)
    @MutationMapping
    public TransactionOperationResult updateTransaction(
            @Argument UUID uuid,
            @Argument TransactionInput input
    ) {
        var existing = transactionService.get(uuid).orElseThrow();
        var builder = new Transaction.Builder(existing);

        var debitedAccount = accountService.get(input.accountDebitedUuid()).orElseThrow();
        var creditedAccount = accountService.get(input.accountCreditedUuid()).orElseThrow();
        if (input.parentUuid() != null) {
            transactionService.get(input.parentUuid()).orElseThrow();
        }
        if (input.contactUuid() != null) {
            contactService.get(input.contactUuid()).orElseThrow();
        }

        builder.amount(input.amount())
                .creditAmount(input.creditAmount())
                .transactionDate(input.transactionDate())
                .type(input.type())
                .comment(input.comment())
                .checked(input.checked())
                .accountDebitedUuid(debitedAccount.uuid())
                .accountCreditedUuid(creditedAccount.uuid())
                .accountDebitedType(debitedAccount.type())
                .accountCreditedType(creditedAccount.type())
                .accountDebitedCategoryUuid(debitedAccount.categoryUuid())
                .accountCreditedCategoryUuid(creditedAccount.categoryUuid())
                .contactUuid(input.contactUuid())
                .invoiceNumber(input.invoiceNumber())
                .parentUuid(input.parentUuid())
                .detailed(input.detailed())
                .statementDate(input.statementDate())
                .modified(System.currentTimeMillis());

        var contact = setupContact(uuid, builder, input);

        var updateResult = transactionService.createOrUpdateTransaction(
                builder.build(),
                contact
        ).orElseThrow(() -> new GraphQLUpdateException("Transaction", uuid));

        var accountIds = new HashSet<UUID>();
        accountIds.add(existing.accountDebitedUuid());
        accountIds.add(existing.accountCreditedUuid());
        accountIds.add(input.accountDebitedUuid());
        accountIds.add(input.accountCreditedUuid());
        var accounts = accountService.updateBalances(accountIds);

        return new TransactionOperationResult(
                updateResult.transaction(),
                updateResult.contact(),
                accounts
        );
    }

    private Contact setupContact(UUID uuid, Transaction.Builder builder, TransactionInput input) {
        if (input.contactUuid() != null) {
            contactService.get(input.contactUuid())
                    .orElseThrow(() -> uuid == null ?
                            new GraphQLCreateException("Transaction") :
                            new GraphQLNotFoundException("Transaction", uuid)
                    );
            builder.contactUuid(input.contactUuid());
            return null;
        } else if (input.contactName() != null) {
            var contact = new Contact.Builder()
                    .name(input.contactName())
                    .type(ContactType.PERSONAL)
                    .build();
            builder.contactUuid(contact.uuid());
            return contact;
        } else {
            return null;
        }
    }

    @SchemaMapping
    public Account accountDebited(Transaction transaction) {
        return accountService.get(transaction.accountDebitedUuid()).orElseThrow();
    }

    @SchemaMapping
    public Account accountCredited(Transaction transaction) {
        return accountService.get(transaction.accountCreditedUuid()).orElseThrow();
    }

    @SchemaMapping
    public Contact contact(Transaction transaction) {
        return transaction.contactUuid() == null ?
                null : contactService.get(transaction.contactUuid()).orElseThrow();
    }

    @SchemaMapping
    public Transaction parent(Transaction transaction) {
        return transaction.parentUuid() == null ?
                null : transactionRepository.get(transaction.parentUuid()).orElseThrow();
    }
}
