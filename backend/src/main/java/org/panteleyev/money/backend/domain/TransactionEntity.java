/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Transaction")
@Table(name = "transaction")
public class TransactionEntity implements MoneyEntity {
    private UUID uuid;
    private BigDecimal amount;
    private BigDecimal creditAmount;
    private LocalDate transactionDate;
    private String type;
    private String comment;
    private boolean checked;
    private String accountDebitedType;
    private String accountCreditedType;
    private AccountEntity accountDebited;
    private AccountEntity accountCredited;
    private CategoryEntity accountDebitedCategory;
    private CategoryEntity accountCreditedCategory;
    private ContactEntity contact;
    private String invoiceNumber;
    private TransactionEntity parent;
    private boolean detailed;
    private LocalDate statementDate;
    private CardEntity card;
    private long created;
    private long modified;

    public TransactionEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public TransactionEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionEntity setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public TransactionEntity setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
        return this;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public TransactionEntity setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
        return this;
    }

    public String getType() {
        return type;
    }

    public TransactionEntity setType(String type) {
        this.type = type;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public TransactionEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public boolean isChecked() {
        return checked;
    }

    public TransactionEntity setChecked(boolean checked) {
        this.checked = checked;
        return this;
    }

    @Column(name = "acc_debited_type", nullable = false)
    public String getAccountDebitedType() {
        return accountDebitedType;
    }

    public TransactionEntity setAccountDebitedType(String accountDebitedType) {
        this.accountDebitedType = accountDebitedType;
        return this;
    }

    @Column(name = "acc_credited_type", nullable = false)
    public String getAccountCreditedType() {
        return accountCreditedType;
    }

    public TransactionEntity setAccountCreditedType(String accountCreditedType) {
        this.accountCreditedType = accountCreditedType;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_debited_uuid", nullable = false)
    public AccountEntity getAccountDebited() {
        return accountDebited;
    }

    public TransactionEntity setAccountDebited(AccountEntity accountDebited) {
        this.accountDebited = accountDebited;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_credited_uuid", nullable = false)
    public AccountEntity getAccountCredited() {
        return accountCredited;
    }

    public TransactionEntity setAccountCredited(AccountEntity accountCredited) {
        this.accountCredited = accountCredited;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_debited_category_uuid", nullable = false)
    public CategoryEntity getAccountDebitedCategory() {
        return accountDebitedCategory;
    }

    public TransactionEntity setAccountDebitedCategory(CategoryEntity accountDebitedCategory) {
        this.accountDebitedCategory = accountDebitedCategory;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_credited_category_uuid", nullable = false)
    public CategoryEntity getAccountCreditedCategory() {
        return accountCreditedCategory;
    }

    public TransactionEntity setAccountCreditedCategory(CategoryEntity accountCreditedCategory) {
        this.accountCreditedCategory = accountCreditedCategory;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_uuid")
    public ContactEntity getContact() {
        return contact;
    }

    public TransactionEntity setContact(ContactEntity contact) {
        this.contact = contact;
        return this;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public TransactionEntity setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_uuid")
    public TransactionEntity getParent() {
        return parent;
    }

    public TransactionEntity setParent(TransactionEntity parent) {
        this.parent = parent;
        return this;
    }

    public boolean isDetailed() {
        return detailed;
    }

    public TransactionEntity setDetailed(boolean detailed) {
        this.detailed = detailed;
        return this;
    }

    public LocalDate getStatementDate() {
        return statementDate;
    }

    public TransactionEntity setStatementDate(LocalDate statementDate) {
        this.statementDate = statementDate;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_uuid")
    public CardEntity getCard() {
        return card;
    }

    public TransactionEntity setCard(CardEntity card) {
        this.card = card;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public TransactionEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public TransactionEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransactionEntity that)) return false;
        return checked == that.checked
                && detailed == that.detailed
                && created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(amount, that.amount)
                && Objects.equals(creditAmount, that.creditAmount)
                && Objects.equals(transactionDate, that.transactionDate)
                && Objects.equals(type, that.type)
                && Objects.equals(comment, that.comment)
                && Objects.equals(accountDebitedType, that.accountDebitedType)
                && Objects.equals(accountCreditedType, that.accountCreditedType)
                && Objects.equals(accountDebited, that.accountDebited)
                && Objects.equals(accountCredited, that.accountCredited)
                && Objects.equals(accountDebitedCategory, that.accountDebitedCategory)
                && Objects.equals(accountCreditedCategory, that.accountCreditedCategory)
                && Objects.equals(contact, that.contact)
                && Objects.equals(invoiceNumber, that.invoiceNumber)
                && Objects.equals(parent, that.parent)
                && Objects.equals(statementDate, that.statementDate)
                && Objects.equals(card, that.card);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, amount, creditAmount, transactionDate, type, comment, checked, accountDebitedType,
                accountCreditedType, accountDebited, accountCredited, accountDebitedCategory, accountCreditedCategory,
                contact, invoiceNumber, parent, detailed, statementDate, card, created, modified);
    }
}
