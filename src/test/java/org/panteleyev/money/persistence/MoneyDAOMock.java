/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.persistence;

import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoneyDAOMock implements RecordSource {
    private final Map<UUID, Category> categories;
    private final Map<UUID, Account> accounts;
    private final Map<UUID, Contact> contacts;
    private final Map<UUID, Currency> currencies;
    private final Map<UUID, Transaction> transactions;

    public MoneyDAOMock(List<Category> categories, List<Account> accounts, List<Contact> contacts,
                        List<Currency> currencies, List<Transaction> transactions)
    {
        this.categories = categories.stream()
            .collect(Collectors.toMap(Category::getGuid, Function.identity()));
        this.accounts = accounts.stream()
            .collect(Collectors.toMap(Account::getGuid, Function.identity()));
        this.contacts = contacts.stream()
            .collect(Collectors.toMap(Contact::getGuid, Function.identity()));
        this.currencies = currencies.stream()
            .collect(Collectors.toMap(Currency::getGuid, Function.identity()));
        this.transactions = transactions.stream()
            .collect(Collectors.toMap(Transaction::getGuid, Function.identity()));
    }

    @Override
    public Optional<Category> getCategory(UUID uuid) {
        return Optional.ofNullable(categories.get(uuid));
    }

    @Override
    public Optional<Currency> getCurrency(UUID uuid) {
        return Optional.ofNullable(currencies.get(uuid));
    }

    @Override
    public Optional<Contact> getContact(UUID uuid) {
        return Optional.ofNullable(contacts.get(uuid));
    }

    @Override
    public Optional<Account> getAccount(UUID uuid) {
        return Optional.ofNullable(accounts.get(uuid));
    }

    @Override
    public Optional<Transaction> getTransaction(UUID uuid) {
        return Optional.ofNullable(transactions.get(uuid));
    }

    @Override
    public List<Transaction> getTransactionDetails(Transaction parent) {
        return transactions.values().stream()
            .filter(t -> Objects.equals(t.getParentUuid().orElse(null),  parent.getGuid()))
            .collect(Collectors.toList());
    }
}
