/*
 * Copyright (c) 2014, 2017, Petr Panteleyev <petr@panteleyev.org>
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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.panteleyev.persistence.DAO;
import org.panteleyev.persistence.Record;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoneyDAO extends DAO {
    private static final MoneyDAO INSTANCE = new MoneyDAO();

    private final Map<Integer, Category> categoriesMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Category> categories =
            FXCollections.observableMap(categoriesMap);

    private final Map<Integer, Contact> contactsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Contact> contacts =
            FXCollections.observableMap(contactsMap);

    private final Map<Integer, Currency> currencyMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Currency> currencies =
            FXCollections.observableMap(currencyMap);

    private final Map<Integer, Account> accountsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Account> accounts =
            FXCollections.observableMap(accountsMap);

    private final Map<Integer, TransactionGroup> transactionGroupsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, TransactionGroup> transactionGroups =
            FXCollections.observableMap(transactionGroupsMap);

    private final Map<Integer, Transaction> transactionsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Transaction> transactions =
            FXCollections.observableMap(transactionsMap);

    private final SimpleBooleanProperty preloadingProperty =
            new SimpleBooleanProperty(false);

    private MoneyDAO() {
    }

    public static MoneyDAO initialize(DataSource ds) {
        INSTANCE.setDataSource(ds);
        return INSTANCE;
    }

    public static MoneyDAO getInstance() {
        return INSTANCE;
    }

    @Override
    public void setDataSource(DataSource ds) {
        super.setDataSource(ds);

        preloadingProperty.set(true);
        categoriesMap.clear();
        contactsMap.clear();
        currencyMap.clear();
        accountsMap.clear();
        transactionGroupsMap.clear();
        transactionsMap.clear();
        preloadingProperty.set(false);
    }

    public BooleanProperty preloadingProperty() {
        return preloadingProperty;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Category> categories() {
        return categories;
    }

    public Optional<Category> getCategory(int id) {
        return id == 0?
                Optional.empty()
                : Optional.ofNullable(categories.get(id));
    }

    public Category insertCategory(Category category) {
        Category result = insert(category);
        categories.put(result.getId(), result);
        return result;
    }

    public Category updateCategory(Category category) {
        Category result = update(category);
        categories.put(result.getId(), result);
        return result;
    }

    public Collection<Category> getCategories() {
        return categories.values();
    }

    public List<Category> getCategoriesByType(CategoryType... types) {
        List<CategoryType> typeList = Arrays.asList(types);

        return getCategories().stream()
                .filter(c -> typeList.contains(c.getType()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Currency> currencies() {
        return currencies;
    }

    public Optional<Currency> getCurrency(int id) {
        return id == 0?
                Optional.empty()
                : Optional.ofNullable(currencies.get(id));
    }

    public Currency insertCurrency(Currency currency) {
        Currency result = insert(currency);
        currencies.put(result.getId(), result);
        return result;
    }

    public Currency updateCurrency(Currency currency) {
        Currency result = update(currency);
        currencies.put(result.getId(), result);
        return result;
    }

    public Collection<Currency> getCurrencies() {
        return currencies.values();
    }

    public Optional<Currency> getDefaultCurrency() {
        return getCurrencies().stream()
                .filter(Currency::isDef)
                .findAny();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Contact> contacts() {
        return contacts;
    }

    public Optional<Contact> getContact(Integer id) {
        return id == null?
                Optional.empty()
                : Optional.ofNullable(contacts.get(id));
    }

    public Contact insertContact(Contact contact) {
        Contact result = insert(contact);
        contacts.put(result.getId(), result);
        return result;
    }

    public Contact updateContact(Contact contact) {
        Contact result = update(contact);
        contacts.put(result.getId(), result);
        return result;
    }

    public Collection<Contact> getContacts() {
        return contacts.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Account> accounts() {
        return accounts;
    }

    public Optional<Account> getAccount(int id) {
        return id == 0?
                Optional.empty()
                : Optional.ofNullable(accounts.get(id));
    }

    public Account insertAccount(Account account) {
        Account result = insert(account);
        accounts.put(result.getId(), result);
        return result;
    }

    public Account updateAccount(Account account) {
        Account result = update(account);
        accounts.put(result.getId(), result);
        return result;
    }

    public void deleteAccount(Account account) {
        accounts.remove(account.getId());
        delete(account);
    }

    public Collection<Account> getAccounts() {
        return accounts.values();
    }

    public List<Account> getAccountsByType(CategoryType type) {
        return accounts.values().stream()
                .filter(a -> a.getType().equals(type))
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategory(int id) {
        return accounts.values().stream()
                .filter(a -> a.getCategoryId() == id)
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategoryId(Integer... ids) {
        List<Integer> idList = Arrays.asList(ids);

        return accounts.values().stream()
                .filter(a -> idList.contains(a.getCategoryId()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transaction Groups
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, TransactionGroup> transactionGroups() {
        return transactionGroups;
    }

    public Optional<TransactionGroup> getTransactionGroup(int id) {
        return id == 0?
                Optional.empty()
                : Optional.ofNullable(transactionGroups.get(id));
    }

    public TransactionGroup insertTransactionGroup(TransactionGroup tg) {
        TransactionGroup result = insert(tg);
        transactionGroups.put(result.getId(), result);
        return result;
    }

    public TransactionGroup updateTransactionGroup(TransactionGroup tg) {
        TransactionGroup result = update(tg);
        transactionGroups.put(result.getId(), result);
        return result;
    }

    public void deleteTransactionGroup(int id) {
        transactionGroups.remove(id);
        delete(id, TransactionGroup.class);
    }

    public Collection<TransactionGroup> getTransactionGroups() {
        return transactionGroups.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Transaction> transactions() {
        return transactions;
    }

    public Optional<Transaction> getTransaction(int id) {
        return id == 0?
                Optional.empty()
                : Optional.ofNullable(transactions.get(id));
    }

    public Transaction insertTransaction(Transaction transaction) {
        Transaction result = insert(transaction);
        transactions.put(result.getId(), result);
        return result;
    }

    public Transaction updateTransaction(Transaction transaction) {
        Transaction result = update(transaction);
        transactions.put(result.getId(), result);
        return result;
    }

    public void deleteTransaction(int id) {
        transactions.remove(id);
        delete(id, Transaction.class);
    }

    public Collection<Transaction> getTransactions() {
        return transactions.values();
    }

    public List<Transaction> getTransactions(Collection<Account> accounts) {
        List<Integer> ids = accounts.stream().map(Account::getId).collect(Collectors.toList());

        return getTransactions().stream()
                .filter(t -> ids.contains(t.getAccountDebitedId()) || ids.contains(t.getAccountCreditedId()))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(int month, int year) {
        return getTransactions().stream()
                .filter(t -> t.getMonth() == month && t.getYear() == year)
                .collect(Collectors.toList());
    }

    public Stream<Transaction> getTransactions(Account account) {
        int id = account.getId();

        return getTransactions().stream()
                .filter(t -> t.getAccountDebitedId() == id || t.getAccountCreditedId() == id);
    }

    public List<Transaction> getTransactionsByCategories(Collection<Category> categories) {
        List<Integer> ids = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        return getTransactions().stream()
                .filter(t -> ids.contains(t.getAccountDebitedCategoryId()) || ids.contains(t.getAccountCreditedCategoryId()))
                .collect(Collectors.toList());
    }

    public Set<String> getUniqueTransactionComments() {
        return getTransactions().stream()
                .map(Transaction::getComment)
                .filter(c -> !c.isEmpty())
                .distinct()
                .collect(Collectors.toSet());
    }

    public long getTransactionCount(Account account) {
        int id = account.getId();

        return getTransactions().stream()
                .filter(t -> t.getAccountDebitedId() == id || t.getAccountCreditedId() == id)
                .count();
    }

    private static List<Class<? extends Record>> getTableClasses() {
        return Arrays.asList(
                Category.class,
                Contact.class,
                Currency.class,
                Account.class,
                TransactionGroup.class,
                Transaction.class
        );
    }

    public void createTables() {
        super.createTables(getTableClasses());
    }

    public void preload() {
        preloadingProperty.set(true);

        preload(getTableClasses());

        categoriesMap.clear();
        getAll(Category.class, categoriesMap);

        contactsMap.clear();
        getAll(Contact.class, contactsMap);

        currencyMap.clear();
        getAll(Currency.class, currencyMap);

        accountsMap.clear();
        getAll(Account.class, accountsMap);

        transactionGroupsMap.clear();
        getAll(TransactionGroup.class, transactionGroupsMap);

        transactionsMap.clear();
        getAll(Transaction.class, transactionsMap);

        preloadingProperty.set(false);
    }

    public static boolean isOpen() {
        return getInstance() != null && getInstance().getDataSource() != null;
    }
}
