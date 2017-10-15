/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.test;

import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.RecordSource;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionGroup;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class MoneyDAOMock implements RecordSource {
    private final Map<Integer, Category> categories;
    private final Map<Integer, Account> accounts;
    private final Map<Integer, Contact> contacts;
    private final Map<Integer, Currency> currencies;
    private final Map<Integer, TransactionGroup> transactionGroups;
    private final Map<Integer, Transaction> transactions;

    public MoneyDAOMock(List<Category> categories, List<Account> accounts, List<Contact> contacts, List<Currency>
            currencies, List<TransactionGroup> transactionGroups, List<Transaction> transactions) {
        this.categories = categories.stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));
        this.accounts = accounts.stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        this.contacts = contacts.stream()
                .collect(Collectors.toMap(Contact::getId, Function.identity()));
        this.currencies = currencies.stream()
                .collect(Collectors.toMap(Currency::getId, Function.identity()));
        this.transactionGroups = transactionGroups.stream()
                .collect(Collectors.toMap(TransactionGroup::getId, Function.identity()));
        this.transactions = transactions.stream()
                .collect(Collectors.toMap(Transaction::getId, Function.identity()));
    }

    @Override
    public Optional<Category> getCategory(int id) {
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public Optional<Currency> getCurrency(int id) {
        return Optional.ofNullable(currencies.get(id));
    }

    @Override
    public Optional<Contact> getContact(int id) {
        return Optional.ofNullable(contacts.get(id));
    }

    @Override
    public Optional<Account> getAccount(int id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<TransactionGroup> getTransactionGroup(int id) {
        return Optional.ofNullable(transactionGroups.get(id));
    }

    @Override
    public Optional<Transaction> getTransaction(int id) {
        return Optional.ofNullable(transactions.get(id));
    }
}
