// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDTO;
import org.panteleyev.money.backend.repository.AccountRepository;
import org.panteleyev.money.backend.repository.CardRepository;
import org.panteleyev.money.backend.repository.ContactRepository;
import org.panteleyev.money.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.panteleyev.money.backend.util.JsonUtil.writeStreamAsJsonArray;

@Service
public class TransactionService {
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ContactRepository contactRepository;
    private final CardRepository cardRepository;
    private final EntityToDtoConverter converter;

    public TransactionService(
            ObjectMapper objectMapper,
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            ContactRepository contactRepository,
            CardRepository cardRepository,
            EntityToDtoConverter converter)
    {
        this.objectMapper = objectMapper;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.contactRepository = contactRepository;
        this.cardRepository = cardRepository;
        this.converter = converter;
    }

    public Optional<TransactionFlatDTO> get(UUID uuid) {
        return transactionRepository.findById(uuid).map(converter::entityToFlatDto);
    }

    @Transactional
    public TransactionFlatDTO put(TransactionFlatDTO transaction) {
        var accountDebited = accountRepository.findById(transaction.getAccountDebitedUuid()).orElseThrow();
        var accountCredited = accountRepository.findById(transaction.getAccountCreditedUuid()).orElseThrow();
        var contact = transaction.getContactUuid() == null ?
                null : contactRepository.getReferenceById(transaction.getContactUuid());
        var parent = transaction.getParentUuid() == null ?
                null : transactionRepository.getReferenceById(transaction.getParentUuid());
        var card = transaction.getCardUuid() == null ?
                null : cardRepository.getReferenceById(transaction.getCardUuid());

        return converter.entityToFlatDto(transactionRepository.save(converter.dtoToEntity(
                transaction,
                accountDebited,
                accountCredited,
                contact,
                parent,
                card
        )));
    }

    public List<TransactionFlatDTO> getAll() {
        var l = transactionRepository.findAll();
        return transactionRepository.findAll().stream().map(converter::entityToFlatDto).toList();
    }

    @Transactional(readOnly = true)
    public void streamAll(OutputStream out) {
        try (var stream = transactionRepository.streamAll()) {
            writeStreamAsJsonArray(objectMapper, stream.map(converter::entityToFlatDto), out);
        }
    }

    public List<TransactionFlatDTO> getByYearAndMonth(int year, int month) {
        var dateFrom = LocalDate.of(year, month, 1);
        var dateTo = dateFrom.with(lastDayOfMonth());

        return transactionRepository.findByDateRange(dateFrom, dateTo).stream()
                .map(converter::entityToFlatDto)
                .toList();
    }

//    public Optional<TransactionUpdateResult> createOrUpdateTransaction(
//            Transaction transaction,
//            Contact contact
//    ) {
//        if (contact != null) {
//            if (contactRepository.insertOrUpdate(contact) != 1) {
//                return Optional.empty();
//            }
//        }
//
//        if (transactionRepository.insertOrUpdate(transaction) != 1) {
//            return Optional.empty();
//        }
//
//        return Optional.of(
//                new TransactionUpdateResult(
//                        transaction, contact
//                )
//        );
//    }
}
