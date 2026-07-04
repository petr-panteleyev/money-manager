// Copyright © 2025-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.backend.domain.TransactionEntity;
import org.panteleyev.money.backend.openapi.dto.CardFlatDTO;
import org.panteleyev.money.backend.openapi.dto.CardType;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDTO;
import org.panteleyev.money.backend.openapi.dto.ContactType;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDTO;
import org.panteleyev.money.backend.openapi.dto.IconFlatDTO;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDTO;
import org.panteleyev.money.backend.openapi.dto.TransactionType;
import org.springframework.stereotype.Service;

@Service
public class EntityToDtoConverter {

    // Card

    public CardFlatDTO entityToFlatDto(CardEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CardFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setAccountUuid(entity.getAccount() == null ? null : entity.getAccount().getUuid());
        dto.setType(CardType.fromValue(entity.getType()));
        dto.setNumber(entity.getNumber());
        dto.setExpiration(entity.getExpiration());
        dto.setComment(entity.getComment());
        dto.setEnabled(entity.isEnabled());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CardEntity dtoToEntity(CardFlatDTO dto, AccountEntity accountEntity) {
        if (dto == null) {
            return null;
        }
        return new CardEntity()
                .setUuid(dto.getUuid())
                .setAccount(accountEntity)
                .setType(dto.getType().name())
                .setNumber(dto.getNumber())
                .setExpiration(dto.getExpiration())
                .setComment(dto.getComment())
                .setEnabled(dto.getEnabled())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Icon

    public IconFlatDTO entityToFlatDto(IconEntity entity) {
        if (entity == null) {
            return null;
        }
        var dto = new IconFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setBytes(entity.getBytes());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public IconEntity dtoToEntity(IconFlatDTO dto) {
        if (dto == null) {
            return null;
        }
        return new IconEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setBytes(dto.getBytes())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Category

    public CategoryFlatDTO entityToFlatDto(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CategoryFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setComment(entity.getComment());
        dto.setType(entity.getType());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CategoryEntity dtoToEntity(CategoryFlatDTO dto, IconEntity icon) {
        if (dto == null) {
            return null;
        }
        return new CategoryEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setComment(dto.getComment())
                .setType(dto.getType())
                .setIcon(icon)
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Contact

    public ContactFlatDTO entityToFlatDto(ContactEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new ContactFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setType(ContactType.fromValue(entity.getType()));
        dto.setComment(entity.getComment());
        dto.setPhone(entity.getPhone());
        dto.setMobile(entity.getMobile());
        dto.setEmail(entity.getEmail());
        dto.setWeb(entity.getWeb());
        dto.setStreet(entity.getStreet());
        dto.setCity(entity.getCity());
        dto.setCountry(entity.getCountry());
        dto.setZip(entity.getZip());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public ContactEntity dtoToEntity(ContactFlatDTO dto, IconEntity icon) {
        if (dto == null) {
            return null;
        }
        return new ContactEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setType(dto.getType().name())
                .setComment(dto.getComment())
                .setPhone(dto.getPhone())
                .setMobile(dto.getMobile())
                .setEmail(dto.getEmail())
                .setWeb(dto.getWeb())
                .setStreet(dto.getStreet())
                .setCity(dto.getCity())
                .setCountry(dto.getCountry())
                .setZip(dto.getZip())
                .setIcon(icon)
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Currency

    public CurrencyFlatDTO entityToFlatDto(CurrencyEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CurrencyFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setSymbol(entity.getSymbol());
        dto.setDescription(entity.getDescription());
        dto.setFormatSymbol(entity.getFormatSymbol());
        dto.setFormatSymbolPosition(entity.getFormatSymbolPosition());
        dto.setShowFormatSymbol(entity.isShowFormatSymbol());
        dto.setDef(entity.isDef());
        dto.setRate(entity.getRate());
        dto.setDirection(entity.getDirection());
        dto.setUseThousandSeparator(entity.isUseThousandSeparator());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CurrencyEntity dtoToEntity(CurrencyFlatDTO dto) {
        return new CurrencyEntity()
                .setUuid(dto.getUuid())
                .setSymbol(dto.getSymbol())
                .setDescription(dto.getDescription())
                .setFormatSymbol(dto.getFormatSymbol())
                .setFormatSymbolPosition(dto.getFormatSymbolPosition())
                .setShowFormatSymbol(dto.getShowFormatSymbol())
                .setDef(dto.getDef())
                .setRate(dto.getRate())
                .setDirection(dto.getDirection())
                .setUseThousandSeparator(dto.getUseThousandSeparator())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Transaction

    public TransactionFlatDTO entityToFlatDto(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new TransactionFlatDTO();
        dto.setUuid(entity.getUuid());
        dto.setAmount(entity.getAmount());
        dto.setCreditAmount(entity.getCreditAmount());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setType(TransactionType.fromValue(entity.getType()));
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
        if (dto == null) {
            return null;
        }

        var debitedCategory = accountDebited.getCategory();
        var creditedCategory = accountCredited.getCategory();

        return new TransactionEntity()
                .setUuid(dto.getUuid())
                .setAmount(dto.getAmount())
                .setCreditAmount(dto.getCreditAmount())
                .setTransactionDate(dto.getTransactionDate())
                .setType(dto.getType().name())
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
