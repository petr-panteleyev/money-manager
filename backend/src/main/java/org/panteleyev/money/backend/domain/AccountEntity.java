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

@Entity(name = "Account")
@Table(name = "account")
public class AccountEntity implements MoneyEntity {
    private UUID uuid;
    private String name;
    private String comment;
    private String accountNumber;
    private BigDecimal openingBalance;
    private BigDecimal accountLimit;
    private BigDecimal currencyRate;
    private String type;
    private CategoryEntity category;
    private CurrencyEntity currency;
    private boolean enabled;
    private BigDecimal interest;
    private LocalDate closingDate;
    private IconEntity icon;
    private BigDecimal total;
    private BigDecimal totalWaiting;
    private long created;
    private long modified;

    public AccountEntity() {
    }

    @Id
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public AccountEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public AccountEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public AccountEntity setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Column(name = "number")
    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountEntity setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
        return this;
    }

    @Column(name = "opening")
    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public AccountEntity setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
        return this;
    }

    public BigDecimal getAccountLimit() {
        return accountLimit;
    }

    public AccountEntity setAccountLimit(BigDecimal accountLimit) {
        this.accountLimit = accountLimit;
        return this;
    }

    @Column(name = "rate")
    public BigDecimal getCurrencyRate() {
        return currencyRate;
    }

    public AccountEntity setCurrencyRate(BigDecimal currencyRate) {
        this.currencyRate = currencyRate;
        return this;
    }

    public String getType() {
        return type;
    }

    public AccountEntity setType(String type) {
        this.type = type;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_uuid", nullable = false)
    public CategoryEntity getCategory() {
        return category;
    }

    public AccountEntity setCategory(CategoryEntity category) {
        this.category = category;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_uuid")
    public CurrencyEntity getCurrency() {
        return currency;
    }

    public AccountEntity setCurrency(CurrencyEntity currency) {
        this.currency = currency;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public AccountEntity setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public AccountEntity setInterest(BigDecimal interest) {
        this.interest = interest;
        return this;
    }

    public LocalDate getClosingDate() {
        return closingDate;
    }

    public AccountEntity setClosingDate(LocalDate closingDate) {
        this.closingDate = closingDate;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_uuid")
    public IconEntity getIcon() {
        return icon;
    }

    public AccountEntity setIcon(IconEntity icon) {
        this.icon = icon;
        return this;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public AccountEntity setTotal(BigDecimal total) {
        this.total = total;
        return this;
    }

    public BigDecimal getTotalWaiting() {
        return totalWaiting;
    }

    public AccountEntity setTotalWaiting(BigDecimal totalWaiting) {
        this.totalWaiting = totalWaiting;
        return this;
    }

    public long getCreated() {
        return created;
    }

    public AccountEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    public long getModified() {
        return modified;
    }

    public AccountEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }

    public AccountEntity updateBalance(BigDecimal total, BigDecimal totalWaiting) {
        return setTotal(total)
                .setTotalWaiting(totalWaiting)
                .setModified(System.currentTimeMillis());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AccountEntity that)) return false;
        return enabled == that.enabled
                && created == that.created
                && modified == that.modified
                && Objects.equals(uuid, that.uuid)
                && Objects.equals(name, that.name)
                && Objects.equals(comment, that.comment)
                && Objects.equals(accountNumber, that.accountNumber)
                && Objects.equals(openingBalance, that.openingBalance)
                && Objects.equals(accountLimit, that.accountLimit)
                && Objects.equals(currencyRate, that.currencyRate)
                && Objects.equals(type, that.type)
                && Objects.equals(category, that.category)
                && Objects.equals(currency, that.currency)
                && Objects.equals(interest, that.interest)
                && Objects.equals(closingDate, that.closingDate)
                && Objects.equals(icon, that.icon)
                && Objects.equals(total, that.total)
                && Objects.equals(totalWaiting, that.totalWaiting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, comment, accountNumber, openingBalance, accountLimit, currencyRate, type,
                category, currency, enabled, interest, closingDate, icon, total, totalWaiting, created, modified);
    }
}
