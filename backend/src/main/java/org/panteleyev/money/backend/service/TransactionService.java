/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.model.TransactionUpdateResult;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final ContactRepository contactRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            ContactRepository contactRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.contactRepository = contactRepository;
    }

    public Optional<Transaction> get(UUID uuid) {
        return transactionRepository.get(uuid);
    }

    public Optional<Transaction> put(Transaction transaction) {
        return ServiceUtil.put(transactionRepository, transaction);
    }

    public List<Transaction> getByYearAndMonth(int year, int month) {
        return transactionRepository.getByYearAndMonth(year, month);
    }

    public Optional<TransactionUpdateResult> createOrUpdateTransaction(
            Transaction transaction,
            Contact contact
    ) {
        if (contact != null) {
            if (contactRepository.insertOrUpdate(contact) != 1) {
                return Optional.empty();
            }
        }

        if (transactionRepository.insertOrUpdate(transaction) != 1) {
            return Optional.empty();
        }

        return Optional.of(
                new TransactionUpdateResult(
                        transaction, contact
                )
        );
    }
}
