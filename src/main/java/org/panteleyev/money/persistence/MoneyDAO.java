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
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import org.panteleyev.persistence.DAO;
import org.panteleyev.persistence.Record;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoneyDAO extends DAO {
    private static final MoneyDAO INSTANCE = new MoneyDAO();

    private final SimpleMapProperty<Integer, Category> categoriesProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleMapProperty<Integer, Contact> contactsProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleMapProperty<Integer, Currency> currencyProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleMapProperty<Integer, Account> accountsProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleMapProperty<Integer, TransactionGroup> transactionGroupsProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleMapProperty<Integer, Transaction> transactionsProperty =
            new SimpleMapProperty<>(FXCollections.observableHashMap());

    private final SimpleBooleanProperty preloadingProperty =
            new SimpleBooleanProperty(false);

    private MoneyDAO() {
    }

    public static MoneyDAO initialize(DataSource ds) {
        INSTANCE.setDatasource(ds);
        return INSTANCE;
    }

    public static MoneyDAO getInstance() {
        return INSTANCE;
    }

    @Override
    protected void setDatasource(DataSource ds) {
        super.setDatasource(ds);

        categoriesProperty.set(FXCollections.observableHashMap());
        contactsProperty.set(FXCollections.observableHashMap());
        currencyProperty.set(FXCollections.observableHashMap());
        accountsProperty.set(FXCollections.observableHashMap());
        transactionGroupsProperty.set(FXCollections.observableHashMap());
        transactionsProperty.set(FXCollections.observableHashMap());
    }

    public BooleanProperty preloadingProperty() {
        return preloadingProperty;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, Category> categoriesProperty() {
        return categoriesProperty;
    }

    public Optional<Category> getCategory(Integer id) {
        return Optional.ofNullable(categoriesProperty.get(id));
    }

    public Category insertCategory(Category category) {
        Category result = insert(category);
        categoriesProperty.put(result.getId(), result);
        return result;
    }

    public Category updateCategory(Category category) {
        Category result = update(category);
        categoriesProperty.put(result.getId(), result);
        return result;
    }

    public Collection<Category> getCategories() {
        return categoriesProperty.values();
    }

    public List<Category> getCategoriesByType(CategoryType... types) {
        List<CategoryType> typeList = Arrays.asList(types);

        return getCategories().stream()
                .filter(c -> typeList.contains(c.getCatType()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, Currency> currencyProperty() {
        return currencyProperty;
    }

    public Optional<Currency> getCurrency(Integer id) {
        return Optional.ofNullable(currencyProperty.get(id));
    }

    public Currency insertCurrency(Currency currency) {
        Currency result = insert(currency);
        currencyProperty.put(result.getId(), result);
        return result;
    }

    public Currency updateCurrency(Currency currency) {
        Currency result = update(currency);
        currencyProperty.put(result.getId(), result);
        return result;
    }

    public Collection<Currency> getCurrencies() {
        return currencyProperty.values();
    }

    public Optional<Currency> getDefaultCurrency() {
        return getCurrencies().stream()
                .filter(Currency::isDef)
                .findAny();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, Contact> contactsProperty() {
        return contactsProperty;
    }

    public Optional<Contact> getContact(Integer id) {
        return Optional.ofNullable(contactsProperty.get(id));
    }

    public Contact insertContact(Contact contact) {
        Contact result = insert(contact);
        contactsProperty.put(result.getId(), result);
        return result;
    }

    public Contact updateContact(Contact contact) {
        Contact result = update(contact);
        contactsProperty.put(result.getId(), result);
        return result;
    }

    public Collection<Contact> getContacts() {
        return contactsProperty.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, Account> accountsProperty() {
        return accountsProperty;
    }

    public Optional<Account> getAccount(Integer id) {
        return Optional.ofNullable(accountsProperty.get(id));
    }

    public Account insertAccount(Account account) {
        Account result = insert(account);
        accountsProperty.put(result.getId(), result);
        return result;
    }

    public Account updateAccount(Account account) {
        Account result = update(account);
        accountsProperty.put(result.getId(), result);
        return result;
    }

    public void deleteAccount(Account account) {
        accountsProperty.remove(account.getId());
        delete(account);
    }

    public Collection<Account> getAccounts() {
        return accountsProperty.values();
    }

    public List<Account> getAccountsByType(CategoryType type) {
        return accountsProperty.values().stream()
                .filter(a -> a.getType().equals(type))
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategory(Integer id) {
        return accountsProperty.values().stream()
                .filter(a -> a.getCategoryId().equals(id))
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategoryId(Integer... ids) {
        List<Integer> idList = Arrays.asList(ids);

        return accountsProperty.values().stream()
                .filter(a -> idList.contains(a.getCategoryId()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transaction Groups
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, TransactionGroup> transactionGroupsProperty() {
        return transactionGroupsProperty;
    }

    public Optional<TransactionGroup> getTransactionGroup(Integer id) {
        return Optional.ofNullable(transactionGroupsProperty.get(id));
    }

    public TransactionGroup insertTransactionGroup(TransactionGroup tg) {
        TransactionGroup result = insert(tg);
        transactionGroupsProperty.put(result.getId(), result);
        return result;
    }

    public TransactionGroup updateTransactionGroup(TransactionGroup tg) {
        TransactionGroup result = update(tg);
        transactionGroupsProperty.put(result.getId(), result);
        return result;
    }

    public void deleteTransactionGroup(Integer id) {
        transactionGroupsProperty.remove(id);
        delete(id, TransactionGroup.class);
    }

    public Collection<TransactionGroup> getTransactionGroups() {
        return transactionGroupsProperty.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public SimpleMapProperty<Integer, Transaction> transactionsProperty() {
        return transactionsProperty;
    }

    public Optional<Transaction> getTransaction(Integer id) {
        return Optional.ofNullable(transactionsProperty.get(id));
    }

    public Transaction insertTransaction(Transaction transaction) {
        Transaction result = insert(transaction);
        transactionsProperty.put(result.getId(), result);
        return result;
    }

    public Transaction updateTransaction(Transaction transaction) {
        Transaction result = update(transaction);
        transactionsProperty.put(result.getId(), result);
        return result;
    }

    public void deleteTransaction(Integer id) {
        transactionsProperty.remove(id);
        delete(id, Transaction.class);
    }

    public Collection<Transaction> getTransactions() {
        return transactionsProperty.values();
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

    public List<Transaction> getTransactions(Account account) {
        int id = account.getId();

        return getTransactions().stream()
                .filter(t -> t.getAccountDebitedId() == id || t.getAccountCreditedId() == id)
                .collect(Collectors.toList());
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

        categoriesProperty.set(FXCollections.observableMap(getAll(Category.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        contactsProperty.set(FXCollections.observableMap(getAll(Contact.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        currencyProperty.set(FXCollections.observableMap(getAll(Currency.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        accountsProperty.set(FXCollections.observableMap(getAll(Account.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        transactionGroupsProperty.set(FXCollections.observableMap(getAll(TransactionGroup.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        transactionsProperty.set(FXCollections.observableMap(getAll(Transaction.class)
                .stream().collect(Collectors.toMap(Record::getId, Function.identity()))));

        preloadingProperty.set(false);
    }

    public static boolean isOpen() {
        return getInstance() != null && getInstance().getDataSource() != null;
    }

    /**
     * Special insert method that call no listeners. Used for database import.
     * @param record record
     */
    public void directInsert(Record record) {
        Objects.requireNonNull(record.getId());

        try (Connection conn = getDataSource().getConnection();
             PreparedStatement ps = getPreparedStatement(record, conn, false)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
