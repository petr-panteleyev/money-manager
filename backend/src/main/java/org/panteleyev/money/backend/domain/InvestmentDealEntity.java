// Copyright © 2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.panteleyev.money.backend.openapi.dto.InvestmentDealType;
import org.panteleyev.money.backend.openapi.dto.InvestmentMarketType;
import org.panteleyev.money.backend.openapi.dto.InvestmentOperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "InvestmentDeal")
@Table(name = "investment_deal")
public class InvestmentDealEntity implements MoneyEntity {
    private UUID uuid;
    private AccountEntity account;
    private ExchangeSecurityEntity security;
    private CurrencyEntity currency;
    private String dealNumber;
    private LocalDateTime dealDate;
    private LocalDateTime accountingDate;
    private InvestmentMarketType marketType;
    private InvestmentOperationType operationType;
    private int securityAmount;
    private BigDecimal price;
    private BigDecimal aci;
    private BigDecimal dealVolume;
    private BigDecimal rate;
    private BigDecimal exchangeFee;
    private BigDecimal brokerFee;
    private BigDecimal amount;
    private InvestmentDealType dealType;
    private long created;
    private long modified;

    public InvestmentDealEntity() {
    }

    @Id
    @Column(nullable = false)
    @Override
    public UUID getUuid() {
        return uuid;
    }

    public InvestmentDealEntity setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_uuid", nullable = false)
    public AccountEntity getAccount() {
        return account;
    }

    public InvestmentDealEntity setAccount(AccountEntity account) {
        this.account = account;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_uuid")
    public ExchangeSecurityEntity getSecurity() {
        return security;
    }

    public InvestmentDealEntity setSecurity(ExchangeSecurityEntity security) {
        this.security = security;
        return this;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency_uuid")
    public CurrencyEntity getCurrency() {
        return currency;
    }

    public InvestmentDealEntity setCurrency(CurrencyEntity currency) {
        this.currency = currency;
        return this;
    }

    @Column(nullable = false)
    public String getDealNumber() {
        return dealNumber;
    }

    public InvestmentDealEntity setDealNumber(String dealNumber) {
        this.dealNumber = dealNumber;
        return this;
    }

    @Column(nullable = false)
    public LocalDateTime getDealDate() {
        return dealDate;
    }

    public InvestmentDealEntity setDealDate(LocalDateTime dealDate) {
        this.dealDate = dealDate;
        return this;
    }

    @Column(nullable = false)
    public LocalDateTime getAccountingDate() {
        return accountingDate;
    }

    public InvestmentDealEntity setAccountingDate(LocalDateTime accountingDate) {
        this.accountingDate = accountingDate;
        return this;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public InvestmentMarketType getMarketType() {
        return marketType;
    }

    public InvestmentDealEntity setMarketType(InvestmentMarketType marketType) {
        this.marketType = marketType;
        return this;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public InvestmentOperationType getOperationType() {
        return operationType;
    }

    public InvestmentDealEntity setOperationType(InvestmentOperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    @Column(nullable = false)
    public int getSecurityAmount() {
        return securityAmount;
    }

    public InvestmentDealEntity setSecurityAmount(int securityAmount) {
        this.securityAmount = securityAmount;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getPrice() {
        return price;
    }

    public InvestmentDealEntity setPrice(BigDecimal price) {
        this.price = price;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getAci() {
        return aci;
    }

    public InvestmentDealEntity setAci(BigDecimal aci) {
        this.aci = aci;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getDealVolume() {
        return dealVolume;
    }

    public InvestmentDealEntity setDealVolume(BigDecimal dealVolume) {
        this.dealVolume = dealVolume;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getRate() {
        return rate;
    }

    public InvestmentDealEntity setRate(BigDecimal rate) {
        this.rate = rate;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getExchangeFee() {
        return exchangeFee;
    }

    public InvestmentDealEntity setExchangeFee(BigDecimal exchangeFee) {
        this.exchangeFee = exchangeFee;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getBrokerFee() {
        return brokerFee;
    }

    public InvestmentDealEntity setBrokerFee(BigDecimal brokerFee) {
        this.brokerFee = brokerFee;
        return this;
    }

    @Column(nullable = false)
    public BigDecimal getAmount() {
        return amount;
    }

    public InvestmentDealEntity setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public InvestmentDealType getDealType() {
        return dealType;
    }

    public InvestmentDealEntity setDealType(InvestmentDealType dealType) {
        this.dealType = dealType;
        return this;
    }

    @Column(nullable = false)
    public long getCreated() {
        return created;
    }

    public InvestmentDealEntity setCreated(long created) {
        this.created = created;
        return this;
    }

    @Column(nullable = false)
    public long getModified() {
        return modified;
    }

    public InvestmentDealEntity setModified(long modified) {
        this.modified = modified;
        return this;
    }
}
