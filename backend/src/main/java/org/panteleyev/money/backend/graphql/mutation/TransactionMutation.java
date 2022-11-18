/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.graphql.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.panteleyev.money.backend.graphql.exception.GraphQLCreateException;
import org.panteleyev.money.backend.graphql.exception.GraphQLNotFoundException;
import org.panteleyev.money.backend.graphql.exception.GraphQLUpdateException;
import org.panteleyev.money.backend.graphql.input.TransactionInput;
import org.panteleyev.money.backend.model.TransactionOperationResult;
import org.panteleyev.money.backend.service.AccountService;
import org.panteleyev.money.backend.service.ContactService;
import org.panteleyev.money.backend.service.TransactionService;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.ContactType;
import org.panteleyev.money.model.Transaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Component
public class TransactionMutation implements GraphQLMutationResolver {
    private final TransactionService transactionService;
    private final AccountService accountService;
    private final ContactService contactService;

    public TransactionMutation(
            TransactionService transactionService,
            AccountService accountService,
            ContactService contactService
    ) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.contactService = contactService;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionOperationResult createTransaction(TransactionInput input) {
        var debitedAccount = accountService.get(input.accountDebitedUuid()).orElseThrow();
        var creditedAccount = accountService.get(input.accountCreditedUuid()).orElseThrow();
        if (input.parentUuid() != null) {
            transactionService.get(input.parentUuid()).orElseThrow();
        }

        var builder = new Transaction.Builder()
                .amount(input.amount())
                .day(input.day())
                .month(input.month())
                .year(input.year())
                .type(input.type())
                .comment(input.comment())
                .checked(input.checked())
                .accountDebitedUuid(debitedAccount.uuid())
                .accountCreditedUuid(creditedAccount.uuid())
                .accountDebitedType(debitedAccount.type())
                .accountCreditedType(creditedAccount.type())
                .accountDebitedCategoryUuid(debitedAccount.categoryUuid())
                .accountCreditedCategoryUuid(creditedAccount.categoryUuid())
                .rate(input.rate())
                .rateDirection(input.rateDirection())
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
    public TransactionOperationResult updateTransaction(UUID uuid, TransactionInput input) {
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
                .day(input.day())
                .month(input.month())
                .year(input.year())
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
                .rate(input.rate())
                .rateDirection(input.rateDirection())
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
}
