// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.desktop.persistence;

import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.CardFlatDTO;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.dto.IconFlatDTO;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.dto.TransactionFlatDTO;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.ExchangeSecurity;
import org.panteleyev.money.model.ExchangeSecuritySplit;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.InvestmentDeal;
import org.panteleyev.money.model.Transaction;

public final class DTOConverter {
    private DTOConverter() {
    }

    //
    // Currency
    //

    public static Currency currencyFromDTO(CurrencyFlatDTO dto) {
        if (dto == null) return null;

        return new Currency.Builder()
                .uuid(dto.getUuid())
                .symbol(dto.getSymbol())
                .description(dto.getDescription())
                .formatSymbol(dto.getFormatSymbol())
                .formatSymbolPosition(dto.getFormatSymbolPosition())
                .showFormatSymbol(dto.getShowFormatSymbol())
                .def(dto.getDef())
                .rate(dto.getRate())
                .direction(dto.getDirection())
                .useThousandSeparator(dto.getUseThousandSeparator())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static CurrencyFlatDTO currencyToDTO(Currency currency) {
        if (currency == null) return null;

        return new CurrencyFlatDTO()
                .uuid(currency.uuid())
                .symbol(currency.symbol())
                .description(currency.description())
                .formatSymbol(currency.formatSymbol())
                .formatSymbolPosition(currency.formatSymbolPosition())
                .showFormatSymbol(currency.showFormatSymbol())
                .def(currency.def())
                .rate(currency.rate())
                .direction(currency.direction())
                .useThousandSeparator(currency.useThousandSeparator())
                .created(currency.created())
                .modified(currency.modified());
    }

    //
    // Category
    //

    public static Category categoryFromDTO(CategoryFlatDTO dto) {
        if (dto == null) return null;

        return new Category.Builder()
                .uuid(dto.getUuid())
                .name(dto.getName())
                .comment(dto.getComment())
                .type(dto.getType())
                .iconUuid(dto.getIconUuid())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static CategoryFlatDTO categoryToDTO(Category category) {
        if (category == null) return null;

        return new CategoryFlatDTO()
                .uuid(category.uuid())
                .name(category.name())
                .comment(category.comment())
                .type(category.type())
                .iconUuid(category.iconUuid())
                .created(category.created())
                .modified(category.modified());
    }

    //
    // Contact
    //

    public static Contact contactFromDTO(ContactFlatDTO dto) {
        if (dto == null) return null;

        return new Contact.Builder()
                .uuid(dto.getUuid())
                .name(dto.getName())
                .type(dto.getType())
                .phone(dto.getPhone())
                .mobile(dto.getMobile())
                .email(dto.getEmail())
                .web(dto.getWeb())
                .comment(dto.getComment())
                .street(dto.getStreet())
                .city(dto.getCity())
                .country(dto.getCountry())
                .zip(dto.getZip())
                .iconUuid(dto.getIconUuid())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static ContactFlatDTO contactToDTO(Contact contact) {
        if (contact == null) return null;

        return new ContactFlatDTO()
                .uuid(contact.uuid())
                .name(contact.name())
                .type(contact.type())
                .comment(contact.comment())
                .phone(contact.phone())
                .mobile(contact.mobile())
                .email(contact.email())
                .web(contact.web())
                .street(contact.street())
                .city(contact.city())
                .country(contact.country())
                .zip(contact.zip())
                .iconUuid(contact.iconUuid())
                .created(contact.created())
                .modified(contact.modified());
    }

    //
    // Account
    //

    public static Account accountFromDTO(AccountFlatDTO dto) {
        if (dto == null) return null;

        return new Account.Builder()
                .uuid(dto.getUuid())
                .name(dto.getName())
                .comment(dto.getComment())
                .accountNumber(dto.getAccountNumber())
                .openingBalance(dto.getOpeningBalance())
                .accountLimit(dto.getAccountLimit())
                .currencyRate(dto.getCurrencyRate())
                .type(dto.getType())
                .categoryUuid(dto.getCategoryUuid())
                .currencyUuid(dto.getCurrencyUuid())
                .securityUuid(dto.getSecurityUuid())
                .enabled(dto.getEnabled())
                .interest(dto.getInterest())
                .closingDate(dto.getClosingDate())
                .iconUuid(dto.getIconUuid())
                .total(dto.getTotal())
                .totalWaiting(dto.getTotalWaiting())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static AccountFlatDTO accountToDTO(Account account) {
        if (account == null) return null;

        return new AccountFlatDTO()
                .uuid(account.uuid())
                .name(account.name())
                .comment(account.comment())
                .accountNumber(account.accountNumber())
                .openingBalance(account.openingBalance())
                .accountLimit(account.accountLimit())
                .currencyRate(account.currencyRate())
                .type(account.type())
                .categoryUuid(account.categoryUuid())
                .currencyUuid(account.currencyUuid())
                .securityUuid(account.securityUuid())
                .enabled(account.enabled())
                .interest(account.interest())
                .closingDate(account.closingDate())
                .iconUuid(account.iconUuid())
                .total(account.total())
                .totalWaiting(account.totalWaiting())
                .created(account.created())
                .modified(account.modified());
    }

    //
    // Icon
    //

    public static Icon iconFromDTO(IconFlatDTO dto) {
        if (dto == null) return null;

        return new Icon(
                dto.getUuid(),
                dto.getName(),
                dto.getBytes(),
                dto.getCreated(),
                dto.getModified()
        );
    }

    public static IconFlatDTO iconToDTO(Icon icon) {
        if (icon == null) return null;

        return new IconFlatDTO()
                .uuid(icon.uuid())
                .name(icon.name())
                .bytes(icon.bytes())
                .created(icon.created())
                .modified(icon.modified());
    }

    //
    // Card
    //

    public static Card cardFromDTO(CardFlatDTO dto) {
        if (dto == null) return null;

        return new Card(
                dto.getUuid(),
                dto.getAccountUuid(),
                dto.getType(),
                dto.getNumber(),
                dto.getExpiration(),
                dto.getComment(),
                dto.getEnabled(),
                dto.getCreated(),
                dto.getModified()
        );
    }

    public static CardFlatDTO cardToDTO(Card card) {
        if (card == null) return null;

        return new CardFlatDTO()
                .uuid(card.uuid())
                .accountUuid(card.accountUuid())
                .type(card.type())
                .number(card.number())
                .expiration(card.expiration())
                .comment(card.comment())
                .enabled(card.enabled())
                .created(card.created())
                .modified(card.modified());
    }

    //
    // Transaction
    //

    public static Transaction transactionFromDTO(TransactionFlatDTO dto) {
        if (dto == null) return null;

        return new Transaction(
                dto.getUuid(),
                dto.getAmount(),
                dto.getCreditAmount(),
                dto.getTransactionDate(),
                dto.getType(),
                dto.getComment(),
                dto.getChecked(),
                dto.getAccountDebitedUuid(),
                dto.getAccountCreditedUuid(),
                dto.getAccountDebitedType(),
                dto.getAccountCreditedType(),
                dto.getAccountDebitedCategoryUuid(),
                dto.getAccountCreditedCategoryUuid(),
                dto.getContactUuid(),
                dto.getInvoiceNumber(),
                dto.getParentUuid(),
                dto.getDetailed(),
                dto.getStatementDate(),
                dto.getCardUuid(),
                dto.getLocation(),
                dto.getCreated(),
                dto.getModified()
        );
    }

    public static TransactionFlatDTO transactionToDTO(Transaction transaction) {
        if (transaction == null) return null;

        return new TransactionFlatDTO()
                .uuid(transaction.uuid())
                .amount(transaction.amount())
                .creditAmount(transaction.creditAmount())
                .transactionDate(transaction.transactionDate())
                .type(transaction.type())
                .comment(transaction.comment())
                .checked(transaction.checked())
                .accountDebitedUuid(transaction.accountDebitedUuid())
                .accountCreditedUuid(transaction.accountCreditedUuid())
                .accountDebitedType(transaction.accountDebitedType())
                .accountCreditedType(transaction.accountCreditedType())
                .accountDebitedCategoryUuid(transaction.accountDebitedCategoryUuid())
                .accountCreditedCategoryUuid(transaction.accountCreditedCategoryUuid())
                .contactUuid(transaction.contactUuid())
                .invoiceNumber(transaction.invoiceNumber())
                .parentUuid(transaction.parentUuid())
                .detailed(transaction.detailed())
                .statementDate(transaction.statementDate())
                .cardUuid(transaction.cardUuid())
                .location(transaction.location())
                .created(transaction.created())
                .modified(transaction.modified());
    }

    //
    // InvestmentDeal
    //

    public static InvestmentDeal investmentDealFromDTO(InvestmentDealFlatDTO dto) {
        if (dto == null) return null;

        return new InvestmentDeal(
                dto.getUuid(),
                dto.getAccountUuid(),
                dto.getSecurityUuid(),
                dto.getCurrencyUuid(),
                dto.getDealNumber(),
                dto.getDealDate(),
                dto.getAccountingDate(),
                dto.getMarketType(),
                dto.getOperationType(),
                dto.getSecurityAmount(),
                dto.getPrice(),
                dto.getAci(),
                dto.getDealVolume(),
                dto.getRate(),
                dto.getExchangeFee(),
                dto.getBrokerFee(),
                dto.getAmount(),
                dto.getDealType(),
                dto.getCreated(),
                dto.getModified()
        );
    }

    public static InvestmentDealFlatDTO investmentDealToDTO(InvestmentDeal deal) {
        if (deal == null) return null;

        return new InvestmentDealFlatDTO()
                .uuid(deal.uuid())
                .accountUuid(deal.accountUuid())
                .securityUuid(deal.securityUuid())
                .currencyUuid(deal.currencyUuid())
                .dealNumber(deal.dealNumber())
                .dealDate(deal.dealDate())
                .accountingDate(deal.accountingDate())
                .marketType(deal.marketType())
                .operationType(deal.operationType())
                .securityAmount(deal.securityAmount())
                .price(deal.price())
                .aci(deal.aci())
                .dealVolume(deal.dealVolume())
                .rate(deal.rate())
                .exchangeFee(deal.exchangeFee())
                .brokerFee(deal.brokerFee())
                .amount(deal.amount())
                .dealType(deal.dealType())
                .created(deal.created())
                .modified(deal.modified());
    }

    //
    // ExchangeSecurity
    //

    public static ExchangeSecurity exchangeSecurityFromDTO(ExchangeSecurityFlatDTO dto) {
        if (dto == null) return null;

        return new ExchangeSecurity.Builder()
                .uuid(dto.getUuid())
                .secId(dto.getSecId())
                .name(dto.getName())
                .shortName(dto.getShortName())
                .isin(dto.getIsin())
                .regNumber(dto.getRegNumber())
                .faceValue(dto.getFaceValue())
                .issueDate(dto.getIssueDate())
                .matDate(dto.getMatDate())
                .daysToRedemption(dto.getDaysToRedemption())
                .group(dto.getGroupType())
                .groupName(dto.getGroupName())
                .type(dto.getType())
                .typeName(dto.getTypeName())
                .marketValue(dto.getMarketValue())
                .couponValue(dto.getCouponValue())
                .couponPercent(dto.getCouponPercent())
                .couponDate(dto.getCouponDate())
                .couponFrequency(dto.getCouponFrequency())
                .accruedInterest(dto.getAccruedInterest())
                .couponPeriod(dto.getCouponPeriod())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static ExchangeSecurityFlatDTO exchangeSecurityToDTO(ExchangeSecurity security) {
        if (security == null) return null;

        return new ExchangeSecurityFlatDTO()
                .uuid(security.uuid())
                .secId(security.secId())
                .name(security.name())
                .shortName(security.shortName())
                .isin(security.isin())
                .regNumber(security.regNumber())
                .faceValue(security.faceValue())
                .issueDate(security.issueDate())
                .matDate(security.matDate())
                .daysToRedemption(security.daysToRedemption())
                .groupType(security.group())
                .groupName(security.groupName())
                .type(security.type())
                .typeName(security.typeName())
                .marketValue(security.marketValue())
                .couponValue(security.couponValue())
                .couponPercent(security.couponPercent())
                .couponDate(security.couponDate())
                .couponFrequency(security.couponFrequency())
                .accruedInterest(security.accruedInterest())
                .couponPeriod(security.couponPeriod())
                .created(security.created())
                .modified(security.modified());
    }

    //
    // ExchangeSecuritySplit
    //

    public static ExchangeSecuritySplit exchangeSecuritySplitFromDTO(ExchangeSecuritySplitFlatDTO dto) {
        if (dto == null) return null;

        return new ExchangeSecuritySplit.Builder()
                .uuid(dto.getUuid())
                .securityUuid(dto.getSecurityUuid())
                .type(dto.getSplitType())
                .date(dto.getSplitDate())
                .rate(dto.getRate())
                .comment(dto.getComment())
                .created(dto.getCreated())
                .modified(dto.getModified())
                .build();
    }

    public static ExchangeSecuritySplitFlatDTO exchangeSecuritySplitToDTO(ExchangeSecuritySplit split) {
        if (split == null) return null;

        return new ExchangeSecuritySplitFlatDTO()
                .uuid(split.uuid())
                .securityUuid(split.securityUuid())
                .splitType(split.type())
                .splitDate(split.date())
                .rate(split.rate())
                .comment(split.comment())
                .created(split.created())
                .modified(split.modified());
    }
}
