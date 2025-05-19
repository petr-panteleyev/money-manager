/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.service;

import org.panteleyev.money.backend.domain.AccountEntity;
import org.panteleyev.money.backend.domain.CardEntity;
import org.panteleyev.money.backend.domain.CategoryEntity;
import org.panteleyev.money.backend.domain.ContactEntity;
import org.panteleyev.money.backend.domain.CurrencyEntity;
import org.panteleyev.money.backend.domain.DocumentEntity;
import org.panteleyev.money.backend.domain.IconEntity;
import org.panteleyev.money.backend.domain.TransactionEntity;
import org.panteleyev.money.backend.openapi.dto.AccountFlatDto;
import org.panteleyev.money.backend.openapi.dto.CardFlatDto;
import org.panteleyev.money.backend.openapi.dto.CardType;
import org.panteleyev.money.backend.openapi.dto.CategoryFlatDto;
import org.panteleyev.money.backend.openapi.dto.CategoryType;
import org.panteleyev.money.backend.openapi.dto.ContactFlatDto;
import org.panteleyev.money.backend.openapi.dto.ContactType;
import org.panteleyev.money.backend.openapi.dto.CurrencyFlatDto;
import org.panteleyev.money.backend.openapi.dto.DocumentFlatDto;
import org.panteleyev.money.backend.openapi.dto.DocumentType;
import org.panteleyev.money.backend.openapi.dto.IconFlatDto;
import org.panteleyev.money.backend.openapi.dto.TransactionFlatDto;
import org.panteleyev.money.backend.openapi.dto.TransactionType;
import org.springframework.stereotype.Service;

@Service
public class EntityToDtoConverter {

    // Account

    public AccountFlatDto entityToFlatDto(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new AccountFlatDto();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setComment(entity.getComment());
        dto.setAccountNumber(entity.getAccountNumber());
        dto.setOpeningBalance(entity.getOpeningBalance());
        dto.setAccountLimit(entity.getAccountLimit());
        dto.setCurrencyRate(entity.getCurrencyRate());
        dto.setType(CategoryType.fromValue(entity.getCategory().getType()));
        dto.setCategoryUuid(entity.getCategory().getUuid());
        dto.setCurrencyUuid(entity.getCurrency() == null ? null : entity.getCurrency().getUuid());
        dto.setSecurityUuid(null); // TODO: Fix later
        dto.setEnabled(entity.isEnabled());
        dto.setInterest(entity.getInterest());
        dto.setClosingDate(entity.getClosingDate());
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setTotal(entity.getTotal());
        dto.setTotalWaiting(entity.getTotalWaiting());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public AccountEntity dtoToEntity(AccountFlatDto dto, CategoryEntity category, CurrencyEntity currency,
            IconEntity icon)
    {
        if (dto == null) {
            return null;
        }
        return new AccountEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setComment(dto.getComment())
                .setAccountNumber(dto.getAccountNumber())
                .setOpeningBalance(dto.getOpeningBalance())
                .setAccountLimit(dto.getAccountLimit())
                .setCurrencyRate(dto.getCurrencyRate())
                .setType(category.getType())
                .setCategory(category)
                .setCurrency(currency)
                // TODO: exchange security
                .setEnabled(dto.getEnabled())
                .setInterest(dto.getInterest())
                .setClosingDate(dto.getClosingDate())
                .setIcon(icon)
                .setTotal(dto.getTotal())
                .setTotalWaiting(dto.getTotalWaiting())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Card

    public CardFlatDto entityToFlatDto(CardEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CardFlatDto();
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

    public CardEntity dtoToEntity(CardFlatDto dto, AccountEntity accountEntity) {
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

    public IconFlatDto entityToFlatDto(IconEntity entity) {
        if (entity == null) {
            return null;
        }
        var dto = new IconFlatDto();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setBytes(entity.getBytes());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public IconEntity dtoToEntity(IconFlatDto dto) {
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

    public CategoryFlatDto entityToFlatDto(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CategoryFlatDto();
        dto.setUuid(entity.getUuid());
        dto.setName(entity.getName());
        dto.setComment(entity.getComment());
        dto.setType(CategoryType.fromValue(entity.getType()));
        dto.setIconUuid(entity.getIcon() == null ? null : entity.getIcon().getUuid());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public CategoryEntity dtoToEntity(CategoryFlatDto dto, IconEntity icon) {
        if (dto == null) {
            return null;
        }
        return new CategoryEntity()
                .setUuid(dto.getUuid())
                .setName(dto.getName())
                .setComment(dto.getComment())
                .setType(dto.getType().name())
                .setIcon(icon)
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    // Contact

    public ContactFlatDto entityToFlatDto(ContactEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new ContactFlatDto();
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

    public ContactEntity dtoToEntity(ContactFlatDto dto, IconEntity icon) {
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

    public CurrencyFlatDto entityToFlatDto(CurrencyEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new CurrencyFlatDto();
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

    public CurrencyEntity dtoToEntity(CurrencyFlatDto dto) {
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

    public TransactionFlatDto entityToFlatDto(TransactionEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new TransactionFlatDto();
        dto.setUuid(entity.getUuid());
        dto.setAmount(entity.getAmount());
        dto.setCreditAmount(entity.getCreditAmount());
        dto.setTransactionDate(entity.getTransactionDate());
        dto.setType(TransactionType.fromValue(entity.getType()));
        dto.setComment(entity.getComment());
        dto.setChecked(entity.isChecked());
        dto.setAccountDebitedType(CategoryType.fromValue(entity.getAccountDebitedType()));
        dto.setAccountCreditedType(CategoryType.fromValue(entity.getAccountCreditedType()));
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
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public TransactionEntity dtoToEntity(
            TransactionFlatDto dto,
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
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }

    //
    // Document
    //

    public DocumentFlatDto entityToFlatDto(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }

        var dto = new DocumentFlatDto();
        dto.setUuid(entity.getUuid());
        dto.setOwnerUuid(entity.getOwnerUuid());
        dto.setContactUuid(entity.getContact().getUuid());
        dto.setDocumentType(DocumentType.fromValue(entity.getDocumentType()));
        dto.setFileName(entity.getFileName());
        dto.setFileDate(entity.getFileDate());
        dto.setFileSize(entity.getFileSize());
        dto.setMimeType(entity.getMimeType());
        dto.setDescription(entity.getDescription());
        dto.setCreated(entity.getCreated());
        dto.setModified(entity.getModified());
        return dto;
    }

    public DocumentEntity dtoToEntity(DocumentFlatDto dto, ContactEntity contact) {
        if (dto == null) {
            return null;
        }
        return new DocumentEntity()
                .setUuid(dto.getUuid())
                .setOwnerUuid(dto.getOwnerUuid())
                .setContact(contact)
                .setDocumentType(dto.getDocumentType().name())
                .setFileName(dto.getFileName())
                .setFileDate(dto.getFileDate())
                .setFileSize(dto.getFileSize())
                .setMimeType(dto.getMimeType())
                .setDescription(dto.getDescription())
                .setCreated(dto.getCreated())
                .setModified(dto.getModified());
    }
}
