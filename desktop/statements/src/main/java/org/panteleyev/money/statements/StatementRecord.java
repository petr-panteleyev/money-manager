/*
 Copyright © 2017-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class StatementRecord {
    public static class Builder {
        private LocalDate actual;
        private LocalDate execution;
        private String description;
        private String counterParty;
        private String place;
        private String country;
        private String currency;
        private String amount = "0.00";
        private String accountCurrency;
        private String accountAmount = "0.00";

        public StatementRecord build(DataCache cache) {
            if (execution == null) {
                execution = actual;
            }

            if (accountAmount == null) {
                accountAmount = amount;
            }

            var currencyUuid = cache.getCurrencies().stream()
                    .filter(c -> c.description().equalsIgnoreCase(currency)
                            || c.symbol().equalsIgnoreCase(currency))
                    .findAny()
                    .map(Currency::uuid)
                    .orElse(null);

            var accountCurrencyUuid = cache.getCurrencies().stream()
                    .filter(c -> c.description().equalsIgnoreCase(accountCurrency)
                            || c.symbol().equalsIgnoreCase(accountCurrency))
                    .findAny()
                    .map(Currency::uuid)
                    .orElse(null);


            return new StatementRecord(actual, execution, description, counterParty, place, country, currency,
                    currencyUuid, amount, accountCurrency, accountCurrencyUuid, accountAmount);
        }

        public Builder actual(LocalDate actual) {
            this.actual = actual;
            return this;
        }

        public Builder execution(LocalDate execution) {
            this.execution = execution;
            return this;
        }

        public Builder counterParty(String counterParty) {
            this.counterParty = counterParty;
            return this;
        }

        public Builder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public Builder place(String place) {
            this.place = place;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
    }

    private final LocalDate actual;
    private final LocalDate execution;
    private final String description;
    private final String counterParty;
    private final String place;
    private final String country;
    private final String currency;
    private final UUID currencyUuid;
    private final String amount;
    private final String accountCurrency;
    private final UUID accountCurrencyUuid;
    private final String accountAmount;

    // calculated fields
    private final BigDecimal amountDecimal;
    private final BigDecimal accountAmountDecimal;

    // associated transactions
    private final List<Transaction> transactions = new ArrayList<>();

    private StatementRecord(LocalDate actual, LocalDate execution, String description, String counterParty,
            String place,
            String country, String currency, UUID currencyUuid, String amount, String accountCurrency,
            UUID accountCurrencyUuid,
            String accountAmount) {
        this.actual = actual;
        this.execution = execution;
        this.description = description;
        this.counterParty = counterParty;
        this.place = place;
        this.country = country;
        this.currency = currency;
        this.currencyUuid = currencyUuid;
        this.amount = normaliseAmount(amount);
        this.accountCurrency = accountCurrency;
        this.accountCurrencyUuid = accountCurrencyUuid;
        this.accountAmount = normaliseAmount(accountAmount);

        this.amountDecimal = toBigDecimal(this.amount);
        this.accountAmountDecimal = toBigDecimal(this.accountAmount);
    }

    public LocalDate getActual() {
        return actual;
    }

    public LocalDate getExecution() {
        return execution;
    }

    public String getDescription() {
        return description;
    }

    public String getCounterParty() {
        return counterParty;
    }

    public String getPlace() {
        return place;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }

    public String getAccountAmount() {
        return accountAmount;
    }

    public UUID getCurrencyUuid() {
        return currencyUuid;
    }

    public UUID getAccountCurrencyUuid() {
        return accountCurrencyUuid;
    }

    public Optional<BigDecimal> getAmountDecimal() {
        return Optional.ofNullable(amountDecimal);
    }

    public Optional<BigDecimal> getAccountAmountDecimal() {
        return Optional.ofNullable(accountAmountDecimal);
    }

    private static BigDecimal toBigDecimal(String value) {
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof StatementRecord that)) {
            return false;
        }

        return Objects.equals(actual, that.actual)
                && Objects.equals(execution, that.execution)
                && Objects.equals(description, that.description)
                && Objects.equals(counterParty, that.counterParty)
                && Objects.equals(place, that.place)
                && Objects.equals(country, that.country)
                && Objects.equals(currency, that.currency)
                && Objects.equals(amount, that.amount)
                && Objects.equals(accountCurrency, that.accountCurrency)
                && Objects.equals(accountAmount, that.accountAmount);
    }

    public int hashCode() {
        return Objects.hash(actual, execution, description, counterParty, place, country,
                currency, amount, accountCurrency, accountAmount);
    }

    public String toString() {
        return "StatementRecord ["
                + "counterParty=" + counterParty + ","
                + "description=" + description + ","
                + "place=" + place + ","
                + "country=" + country + ","
                + "currency=" + currency + ","
                + "actual=" + actual + ","
                + "execution=" + execution + ","
                + "amount=" + amount + ","
                + "amountDecimal=" + amountDecimal
                + "]";
    }

    private static String normaliseAmount(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        for (char ch : str.toCharArray()) {
            switch (ch) {
                case ',':
                    result.append('.');
                    break;
                case ' ':
                case 160:
                    break;
                default:
                    result.append(ch);
            }
        }

        return result.toString();
    }

    public Collection<Transaction> getTransactions() {
        return Collections.unmodifiableCollection(transactions);
    }

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
    }
}
