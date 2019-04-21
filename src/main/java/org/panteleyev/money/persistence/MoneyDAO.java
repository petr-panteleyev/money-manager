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

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.MoneyRecord;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.xml.Import;
import org.panteleyev.persistence.DAO;
import org.panteleyev.persistence.Record;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MoneyDAO extends DAO implements RecordSource {
    private static final MoneyDAO MONEY_DAO = new MoneyDAO();

    public static MoneyDAO getDao() {
        return MONEY_DAO;
    }

    public static final int FIELD_SCALE = 6;

    private static final int BATCH_SIZE = 1000;

    public static final Consumer<String> IGNORE_PROGRESS = x -> {
    };

    private static final List<Class<? extends Record>> TABLE_CLASSES = List.of(
        Category.class,
        Contact.class,
        Currency.class,
        Account.class,
        Transaction.class
    );

    private static final List<Class<? extends Record>> TABLE_CLASSES_REVERSED = List.of(
        Transaction.class,
        Account.class,
        Currency.class,
        Contact.class,
        Category.class
    );

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

    private final BooleanProperty preloadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty preloadingProperty() {
        return preloadingProperty;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<UUID, Category> categories() {
        return categories;
    }

    public Optional<Category> getCategory(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(categoriesMap.get(uuid));
    }

    public void insertCategory(Category category) {
        insert(category);
        categories.put(category.getGuid(), category);
    }

    public void updateCategory(Category category) {
        update(category);
        categories.put(category.getGuid(), category);
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

    public ObservableMap<UUID, Currency> currencies() {
        return currencies;
    }

    public Optional<Currency> getCurrency(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(currencyMap.get(uuid));
    }

    public void insertCurrency(Currency currency) {
        insert(currency);
        currencies.put(currency.getGuid(), currency);
    }

    public void updateCurrency(Currency currency) {
        update(currency);
        currencies.put(currency.getGuid(), currency);
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

    public ObservableMap<UUID, Contact> contacts() {
        return contacts;
    }

    public Optional<Contact> getContact(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(contactsMap.get(uuid));
    }

    public void insertContact(Contact contact) {
        insert(contact);
        contacts.put(contact.getGuid(), contact);
    }

    public void updateContact(Contact contact) {
        update(contact);
        contacts.put(contact.getGuid(), contact);
    }

    public Collection<Contact> getContacts() {
        return contactsMap.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<UUID, Account> accounts() {
        return accounts;
    }

    public Optional<Account> getAccount(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(accountsMap.get(uuid));
    }

    public void insertAccount(Account account) {
        insert(account);
        accounts.put(account.getGuid(), account);
    }

    public void updateAccount(Account account) {
        update(account);
        accounts.put(account.getGuid(), account);
    }

    public void deleteAccount(Account account) {
        accounts.remove(account.getGuid());
        delete(account);
    }

    public Collection<Account> getAccounts() {
        return accountsMap.values();
    }

    public Collection<Account> getAccounts(Predicate<Account> filter) {
        return accountsMap.values().stream().filter(filter).collect(Collectors.toList());
    }

    public List<Account> getAccountsByType(CategoryType type) {
        return accountsMap.values().stream()
            .filter(account -> account.getType() == type)
            .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategory(UUID uuid) {
        return accountsMap.values().stream()
            .filter(account -> account.getCategoryUuid().equals(uuid))
            .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategoryId(UUID... ids) {
        var catIDs = List.of(ids);

        return accountsMap.values().stream()
            .filter(account -> catIDs.contains(account.getCategoryUuid()))
            .collect(Collectors.toList());
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountsMap.values().stream()
            .filter(a -> Objects.equals(a.getAccountNumberNoSpaces(), accountNumber))
            .findFirst();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<UUID, Transaction> transactions() {
        return transactions;
    }

    public Optional<Transaction> getTransaction(UUID uuid) {
        return uuid == null ? Optional.empty() : Optional.ofNullable(transactionsMap.get(uuid));
    }

    public void insertTransaction(Transaction transaction) {
        insert(transaction);
        transactions.put(transaction.getGuid(), transaction);
    }

    public void updateTransaction(Transaction transaction) {
        update(transaction);
        transactions.put(transaction.getGuid(), transaction);
    }

    public void deleteTransaction(UUID uuid) {
        // Temporary
        var t = transactions.get(uuid);
        transactions.remove(uuid);
        delete(t);
    }

    public Collection<Transaction> getTransactions() {
        return transactions.values();
    }

    public List<Transaction> getTransactions(Collection<Account> accounts) {
        var ids = accounts.stream()
            .map(Account::getGuid)
            .collect(Collectors.toList());

        return getTransactions().stream()
            .filter(tr -> ids.contains(tr.getAccountDebitedUuid()) || ids.contains(tr.getAccountCreditedUuid()))
            .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getTransactionDetails(Transaction parent) {
        return getTransactions().stream()
            .filter(t -> Objects.equals(t.getParentUuid().orElse(null), parent.getGuid()))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(int month, int year) {
        return getTransactions().stream()
            .filter(tr -> tr.getMonth() == month && tr.getYear() == year)
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(Account account) {
        var uuid = account.getGuid();
        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.getAccountDebitedUuid(), uuid)
                || Objects.equals(tr.getAccountCreditedUuid(), uuid))
            .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByCategories(Collection<Category> categories) {
        var uuids = categories.stream()
            .map(Category::getGuid)
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
        var uuid = account.getGuid();

        return getTransactions().stream()
            .filter(tr -> Objects.equals(tr.getAccountDebitedUuid(), uuid)
                || Objects.equals(tr.getAccountCreditedUuid(), uuid))
            .count();
    }

    public void createTables(Connection conn) {
        super.createTables(conn, TABLE_CLASSES);
    }

    public void createTables() {
        super.createTables(TABLE_CLASSES);
    }

    public void dropTables() {
        super.dropTables(TABLE_CLASSES_REVERSED);
    }

    public void preload() {
        preload(IGNORE_PROGRESS);
    }

    public void preload(Consumer<String> progress) {
        synchronized (preloadingProperty) {
            preloadingProperty.set(true);

            progress.accept("Preloading primary keys... ");
            preload(TABLE_CLASSES);
            progress.accept(" done\n");

            progress.accept("Preloading data...\n");

            progress.accept("    categories... ");
            getAll(Category.class, categoriesMap);
            progress.accept("done\n");

            progress.accept("    contacts... ");
            getAll(Contact.class, contactsMap);
            progress.accept("done\n");

            progress.accept("    currencies... ");
            getAll(Currency.class, currencyMap);
            progress.accept("done\n");

            progress.accept("    accounts... ");
            getAll(Account.class, accountsMap);
            progress.accept("done\n");

            progress.accept("    transactions... ");
            getAll(Transaction.class, transactionsMap);
            progress.accept("done\n");

            progress.accept("done\n");
            preloadingProperty.set(false);
        }
    }

    private void deleteAll(Connection conn, List<Class<? extends Record>> tables) {
        truncate(conn, tables);
        tables.forEach(t -> {
//            deleteAll(conn, t);
//            resetPrimaryKey(t);
        });
    }

    public void initialize(DataSource ds) {
        synchronized (preloadingProperty) {
            setDataSource(ds, DatabaseType.MYSQL);

            preloadingProperty.set(true);
            categoriesMap.clear();
            contactsMap.clear();
            currencyMap.clear();
            accountsMap.clear();
            transactionsMap.clear();
            preloadingProperty.set(false);
        }
    }

    public boolean isOpen() {
        return getDataSource() != null;
    }

    public void importFullDump(Import imp, Consumer<String> progress) {
        try (var conn = getDataSource().getConnection()) {
            progress.accept("Recreating tables... ");
            createTables(conn);
            progress.accept(" done\n");

            progress.accept("Importing data...\n");

            progress.accept("    categories... ");
            insert(conn, BATCH_SIZE, imp.getCategories());
            progress.accept("done\n");

            progress.accept("    currencies... ");
            insert(conn, BATCH_SIZE, imp.getCurrencies());
            progress.accept("done\n");

            progress.accept("    accounts... ");
            insert(conn, BATCH_SIZE, imp.getAccounts());
            progress.accept("done\n");

            progress.accept("    contacts... ");
            insert(conn, BATCH_SIZE, imp.getContacts());
            progress.accept("done\n");

            progress.accept("    transactions... ");
            insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.getParentUuid().isEmpty()).collect(Collectors.toList()));
            insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.getParentUuid().isPresent()).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("done\n");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    private void calculateActions(Map<UUID, ImportAction> idMap,
                                  Map<UUID, ? extends MoneyRecord> existing,
                                  List<? extends MoneyRecord> toImport)
    {
        for (MoneyRecord record : toImport) {
            var found = existing.get(record.getGuid());
            if (found == null) {
                idMap.put(record.getGuid(), ImportAction.INSERT);
            } else {
                if (record.getModified() > found.getModified()) {
                    idMap.put(record.getGuid(), ImportAction.UPDATE);
                } else {
                    idMap.put(record.getGuid(), ImportAction.IGNORE);
                }
            }
        }
    }

    private <T extends MoneyRecord> void importTable(Connection conn,
                                                     List<? extends T> toImport,
                                                     Map<UUID, ImportAction> importActions)
    {
        for (T item : toImport) {
            switch(importActions.get(item.getGuid())) {
                case IGNORE:
                    continue;

                case INSERT:
                    insert(conn, item);
                    break;

                case UPDATE:
                    update(conn, item);
                    break;
            }
        }
    }

    public void importRecords(Import imp, Consumer<String> progress) {
        var categoryActions = new HashMap<UUID, ImportAction>();
        var currencyActions = new HashMap<UUID, ImportAction>();
        var contactActions = new HashMap<UUID, ImportAction>();
        var accountActions = new HashMap<UUID, ImportAction>();
        var transactionActions = new HashMap<UUID, ImportAction>();

        calculateActions(currencyActions, currencyMap, imp.getCurrencies());
        calculateActions(categoryActions, categoriesMap, imp.getCategories());
        calculateActions(contactActions, contactsMap, imp.getContacts());
        calculateActions(accountActions, accountsMap, imp.getAccounts());
        calculateActions(transactionActions, transactionsMap, imp.getTransactions());

        try (var conn = getDataSource().getConnection()) {
            try {
                conn.setAutoCommit(false);

                importTable(conn, imp.getCategories(), categoryActions);
                importTable(conn, imp.getCurrencies(), currencyActions);
                importTable(conn, imp.getContacts(), contactActions);
                importTable(conn, imp.getAccounts(), accountActions);
                importTable(conn, imp.getTransactions(), transactionActions);

                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Exception initDatabase(MysqlDataSource dataSource, String schema) {
        dataSource.setDatabaseName(null);

        try (var conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE DATABASE " + schema + " CHARACTER SET = utf8");

            dataSource.setDatabaseName(schema);

            var dao = new DAO(dataSource, DatabaseType.MYSQL);
            dao.createTables(TABLE_CLASSES);

            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }
}
