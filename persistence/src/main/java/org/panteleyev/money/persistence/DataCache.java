/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataCache {
    private static final DataCache INSTANCE = new DataCache();

    private final Map<UUID, Icon> iconsMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Icon> icons = FXCollections.observableMap(iconsMap);

    private final Map<UUID, Category> categoriesMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Category> categories
        = FXCollections.observableMap(categoriesMap);

    private final Map<UUID, Contact> contactsMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Contact> contacts
        = FXCollections.observableMap(contactsMap);

    private final Map<UUID, Currency> currencyMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Currency> currencies
        = FXCollections.observableMap(currencyMap);

    private final Map<UUID, Account> accountsMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Account> accounts
        = FXCollections.observableMap(accountsMap);

    private final Map<UUID, Transaction> transactionsMap = new ConcurrentHashMap<>();
    private final ObservableMap<UUID, Transaction> transactions
        = FXCollections.observableMap(transactionsMap);

    public static DataCache cache() {
        return INSTANCE;
    }

    public void clear() {
        iconsMap.clear();
        categoriesMap.clear();
        contactsMap.clear();
        currencyMap.clear();
        accountsMap.clear();
        transactionsMap.clear();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Icons
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Icon> iconsMap() {
        return iconsMap;
    }

    public ObservableMap<UUID, Icon> icons() {
        return icons;
    }

    public Optional<Icon> getIcon(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(iconsMap.get(uuid));
    }

    public Collection<Icon> getIcons() {
        return iconsMap.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Category> categoriesMap() {
        return categoriesMap;
    }

    public ObservableMap<UUID, Category> categories() {
        return categories;
    }

    public Optional<Category> getCategory(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(categoriesMap.get(uuid));
    }

    public Collection<Category> getCategories() {
        return categoriesMap.values();
    }

    public List<Category> getCategoriesByType(CategoryType... types) {
        var typeList = List.of(types);

        return getCategories().stream()
            .filter(category -> typeList.contains(category.getType()))
            .collect(Collectors.toList());
    }

    public List<Category> getCategoriesByType(EnumSet<CategoryType> types) {
        return getCategories().stream()
            .filter(category -> types.contains(category.getType()))
            .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Currency> currencyMap() {
        return currencyMap;
    }

    public ObservableMap<UUID, Currency> currencies() {
        return currencies;
    }

    public Optional<Currency> getCurrency(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(currencyMap.get(uuid));
    }

    public Collection<Currency> getCurrencies() {
        return currencyMap.values();
    }

    public Optional<Currency> getDefaultCurrency() {
        return getCurrencies().stream().filter(Currency::getDef).findFirst();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Contact> contactsMap() {
        return contactsMap;
    }

    public ObservableMap<UUID, Contact> contacts() {
        return contacts;
    }

    public Optional<Contact> getContact(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(contactsMap.get(uuid));
    }

    public Collection<Contact> getContacts() {
        return contactsMap.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Account> accountsMap() {
        return accountsMap;
    }

    public ObservableMap<UUID, Account> accounts() {
        return accounts;
    }

    public Optional<Account> getAccount(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(accountsMap.get(uuid));
    }

    public Collection<Account> getAccounts() {
        return accountsMap.values();
    }

    public Stream<Account> getAccounts(Predicate<Account> filter) {
        return getAccounts().stream().filter(filter);
    }

    public List<Account> getAccountsByType(CategoryType type) {
        return getAccounts().stream()
            .filter(account -> account.getType() == type)
            .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategory(UUID uuid) {
        return getAccounts().stream()
            .filter(account -> account.getCategoryUuid().equals(uuid))
            .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategoryId(UUID... ids) {
        var catIDs = List.of(ids);

        return getAccounts().stream()
            .filter(account -> catIDs.contains(account.getCategoryUuid()))
            .collect(Collectors.toList());
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return getAccounts().stream()
            .filter(a -> Objects.equals(a.getAccountNumberNoSpaces(), accountNumber))
            .findFirst();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public Map<UUID, Transaction> transactionsMap() {
        return transactionsMap;
    }

    public ObservableMap<UUID, Transaction> transactions() {
        return transactions;
    }

    public Optional<Transaction> getTransaction(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(transactionsMap.get(uuid));
    }

    public Collection<Transaction> getTransactions() {
        return transactions.values();
    }

    public List<Transaction> getTransactions(Collection<Account> accounts) {
        var ids = accounts.stream()
            .map(Account::getUuid)
            .collect(Collectors.toList());

        return getTransactions().stream()
            .filter(tr -> ids.contains(tr.getAccountDebitedUuid()) || ids.contains(tr.getAccountCreditedUuid()))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionDetails(Transaction parent) {
        return getTransactions().stream()
            .filter(t -> Objects.equals(t.getParentUuid().orElse(null), parent.getUuid()))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(int month, int year) {
        return getTransactions().stream()
            .filter(tr -> tr.getMonth() == month && tr.getYear() == year)
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(Account account) {
        var uuid = account.getUuid();
        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.getAccountDebitedUuid(), uuid)
                || Objects.equals(tr.getAccountCreditedUuid(), uuid))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByCategories(Collection<Category> categories) {
        var uuids = categories.stream()
            .map(Category::getUuid)
            .collect(Collectors.toList());

        return getTransactions().stream()
            .filter(tr -> uuids.contains(tr.getAccountDebitedCategoryUuid())
                || uuids.contains(tr.getAccountCreditedCategoryUuid()))
            .collect(Collectors.toList());
    }

    public Set<String> getUniqueTransactionComments() {
        return getTransactions().stream()
            .map(Transaction::getComment)
            .filter(c -> !c.isEmpty())
            .collect(Collectors.toSet());
    }

    public long getTransactionCount(Account account) {
        var uuid = account.getUuid();

        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.getAccountDebitedUuid(), uuid)
                || Objects.equals(tr.getAccountCreditedUuid(), uuid))
            .count();
    }

    public Stream<Transaction> getTransactions(Predicate<Transaction> filter) {
        return getTransactions().stream().filter(filter);
    }

    /**
     * Calculates balance of all transactions related to the specified account.
     *
     * @param total whether initial balance should be added to the result
     * @return account balance
     */
    public BigDecimal calculateBalance(Account account, boolean total, Predicate<Transaction> filter) {
        return getTransactions(account).stream()
            .filter(t -> t.getParentUuid().isEmpty())
            .filter(filter)
            .map(t -> {
                BigDecimal amount = t.getAmount();
                if (Objects.equals(account.getUuid(), t.getAccountCreditedUuid())) {
                    // handle conversion rate
                    var rate = t.getRate();
                    if (rate.compareTo(BigDecimal.ZERO) != 0) {
                        amount = t.getRateDirection() == 0 ?
                            amount.divide(rate, RoundingMode.HALF_UP) : amount.multiply(rate);
                    }
                } else {
                    amount = amount.negate();
                }
                return amount;
            })
            .reduce(total ? account.getOpeningBalance() : BigDecimal.ZERO, BigDecimal::add);
    }
}
