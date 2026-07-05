// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.converter;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.TransactionEntity;
import org.panteleyev.money.dto.TransactionFlatDTO;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter {
    public TransactionFlatDTO entityToFlatDto(TransactionEntity entity) {
        if (entity == null) return null;

        var dto = new TransactionFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setAmount(entity.getAmount());
        dto.setCreditAmount(entity.getCreditAmount());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setType(entity.getType());
        dto.setComment(entity.getComment());
        dto.setChecked(entity.isChecked());
        dto.setAccountDebitedType(entity.getAccountDebitedType());
        dto.setAccountCreditedType(entity.getAccountCreditedType());
        dto.setAccountDebitedUuid(entity.getAccountDebited().getUuid());
        dto.setAccountCreditedUuid(entity.getAccountCredited().getUuid());
        dto.setAccountDebitedCategoryUuid(entity.getAccountDebitedCategory().getUuid());
        dto.setAccountCreditedCategoryUuid(entity.getAccountCreditedCategory().getUuid());
        dto.setContactUuid(entity.getContact() == null ? null : entity.getContact().getUuid());
        dto.setInvoiceNumber(entity.getInvoiceNumber());
        dto.setParentUuid(entity.getParent() == null ? null : entity.getParent().getUuid());
        dto.setDetailed(entity.isDetailed());
        dto.setStatementDate(entity.getStatementDate());
        dto.setCardUuid(entity.getCard() == null ? null : entity.getCard().getUuid());
        dto.setLocation(entity.getLocation());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public TransactionEntity dtoToEntity(
            TransactionFlatDTO dto,
            AccountEntity accountDebited,
            AccountEntity accountCredited,
            ContactEntity contact,
            TransactionEntity parent,
            CardEntity card)
    {
        if (dto == null) return null;

        var debitedCategory = accountDebited.getCategory();
        var creditedCategory = accountCredited.getCategory();

        return new TransactionEntity()
                .setUuid(dto.getUuid())
                .setAmount(dto.getAmount())
                .setCreditAmount(dto.getCreditAmount())
                .setTransactionDate(dto.getTransactionDate())
                .setType(dto.getType())
                .setComment(dto.getComment())
                .setChecked(dto.getChecked())
                .setAccountDebitedType(debitedCategory.getType())
                .setAccountCreditedType(creditedCategory.getType())
                .setAccountDebited(accountDebited)
                .setAccountCredited(accountCredited)
                .setAccountDebitedCategory(debitedCategory)
                .setAccountCreditedCategory(creditedCategory)
                .setContact(contact)
                .setInvoiceNumber(dto.getInvoiceNumber())
                .setParent(parent)
                .setDetailed(dto.getDetailed())
                .setStatementDate(dto.getStatementDate())
                .setCard(card)
                .setLocation(dto.getLocation())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
