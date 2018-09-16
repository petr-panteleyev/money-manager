/*
 * Copyright (c) 2017, 2018, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.util.Pair;
import org.panteleyev.money.persistence.dto.AccountDto;
import org.panteleyev.money.persistence.dto.CategoryDto;
import org.panteleyev.money.persistence.dto.ContactDto;
import org.panteleyev.money.persistence.dto.CurrencyDto;
import org.panteleyev.money.persistence.dto.Dto;
import org.panteleyev.money.persistence.dto.TransactionDto;
import org.panteleyev.money.persistence.dto.TransactionGroupDto;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.MoneyRecord;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.persistence.model.TransactionGroup;
import org.panteleyev.money.xml.Import;
import org.panteleyev.persistence.DAO;
import org.panteleyev.persistence.Record;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.panteleyev.money.persistence.dto.Dto.dtoClass;
import static org.panteleyev.money.persistence.dto.Dto.newDto;

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
            CategoryDto.class,
            ContactDto.class,
            CurrencyDto.class,
            AccountDto.class,
            TransactionGroupDto.class,
            TransactionDto.class
    );

    private static final List<Class<? extends Record>> TABLE_CLASSES_REVERSED = List.of(
            TransactionDto.class,
            TransactionGroupDto.class,
            AccountDto.class,
            CurrencyDto.class,
            ContactDto.class,
            CategoryDto.class
    );

    private final Map<Integer, Category> categoriesMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Category> categories
            = FXCollections.observableMap(categoriesMap);

    private final Map<Integer, Contact> contactsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Contact> contacts
            = FXCollections.observableMap(contactsMap);

    private final Map<Integer, Currency> currencyMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Currency> currencies
            = FXCollections.observableMap(currencyMap);

    private final Map<Integer, Account> accountsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Account> accounts
            = FXCollections.observableMap(accountsMap);

    private final Map<Integer, TransactionGroup> transactionGroupsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, TransactionGroup> transactionGroups
            = FXCollections.observableMap(transactionGroupsMap);

    private final Map<Integer, Transaction> transactionsMap = new ConcurrentHashMap<>();
    private final ObservableMap<Integer, Transaction> transactions
            = FXCollections.observableMap(transactionsMap);

    private final BooleanProperty preloadingProperty = new SimpleBooleanProperty(false);

    private String encryptionKey;

    public BooleanProperty preloadingProperty() {
        return preloadingProperty;
    }

    public void setEncryptionKey(String encryptionKey) {
        Objects.requireNonNull(encryptionKey, "Encryption key cannot be null");
        this.encryptionKey = encryptionKey;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Category> categories() {
        return categories;
    }

    public Optional<Category> getCategory(int id) {
        return Optional.ofNullable(categoriesMap.get(id));
    }

    public void insertCategory(Category category) {
        insert(newDto(category, encryptionKey));
        categories.put(category.getId(), category);
    }

    public void updateCategory(Category category) {
        update(newDto(category, encryptionKey));
        categories.put(category.getId(), category);
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

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Currency> currencies() {
        return currencies;
    }

    public Optional<Currency> getCurrency(int id) {
        return Optional.ofNullable(currencyMap.get(id));
    }

    public void insertCurrency(Currency currency) {
        insert(newDto(currency, encryptionKey));
        currencies.put(currency.getId(), currency);
    }

    public void updateCurrency(Currency currency) {
        update(newDto(currency, encryptionKey));
        currencies.put(currency.getId(), currency);
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

    public ObservableMap<Integer, Contact> contacts() {
        return contacts;
    }

    public Optional<Contact> getContact(int id) {
        return Optional.ofNullable(contactsMap.get(id));
    }

    public void insertContact(Contact contact) {
        insert(newDto(contact, encryptionKey));
        contacts.put(contact.getId(), contact);
    }

    public void updateContact(Contact contact) {
        update(newDto(contact, encryptionKey));
        contacts.put(contact.getId(), contact);
    }

    public Collection<Contact> getContacts() {
        return contactsMap.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Account> accounts() {
        return accounts;
    }

    public Optional<Account> getAccount(int id) {
        return Optional.ofNullable(accountsMap.get(id));
    }

    public void insertAccount(Account account) {
        insert(newDto(account, encryptionKey));
        accounts.put(account.getId(), account);
    }

    public void updateAccount(Account account) {
        update(newDto(account, encryptionKey));
        accounts.put(account.getId(), account);
    }

    public void deleteAccount(Account account) {
        accounts.remove(account.getId());
        delete(account.getId(), dtoClass(Account.class));
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

    public List<Account> getAccountsByCategory(int id) {
        return accountsMap.values().stream()
                .filter(account -> account.getCategoryId() == id)
                .collect(Collectors.toList());
    }

    public List<Account> getAccountsByCategoryId(Integer... ids) {
        var catIDs = List.of(ids);

        return accountsMap.values().stream()
                .filter(account -> catIDs.contains(account.getCategoryId()))
                .collect(Collectors.toList());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transaction Groups
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, TransactionGroup> transactionGroups() {
        return transactionGroups;
    }

    public Optional<TransactionGroup> getTransactionGroup(int id) {
        return id == 0 ? Optional.empty() : Optional.ofNullable(transactionGroupsMap.get(id));
    }

    public void insertTransactionGroup(TransactionGroup tg) {
        insert(newDto(tg, encryptionKey));
        transactionGroups.put(tg.getId(), tg);
    }

    public void updateTransactionGroup(TransactionGroup tg) {
        update(newDto(tg, encryptionKey));
        transactionGroups.put(tg.getId(), tg);
    }

    public void deleteTransactionGroup(int id) {
        transactionGroups.remove(id);
        delete(id, dtoClass(TransactionGroup.class));
    }

    public Collection<TransactionGroup> getTransactionGroups() {
        return transactionGroupsMap.values();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public ObservableMap<Integer, Transaction> transactions() {
        return transactions;
    }

    public Optional<Transaction> getTransaction(int id) {
        return id == 0 ? Optional.empty() : Optional.ofNullable(transactionsMap.get(id));
    }

    public void insertTransaction(Transaction transaction) {
        insert(newDto(transaction, encryptionKey));
        transactions.put(transaction.getId(), transaction);
    }

    public void updateTransaction(Transaction transaction) {
        update(newDto(transaction, encryptionKey));
        transactions.put(transaction.getId(), transaction);
    }

    public void deleteTransaction(int id) {
        transactions.remove(id);
        delete(id, dtoClass(Transaction.class));
    }

    public Collection<Transaction> getTransactions() {
        return transactions.values();
    }

    public List<Transaction> getTransactions(Collection<Account> accounts) {
        var ids = accounts.stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        return getTransactions().stream()
                .filter(tr -> ids.contains(tr.getAccountDebitedId()) || ids.contains(tr.getAccountCreditedId()))
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(int month, int year) {
        return getTransactions().stream()
                .filter(tr -> tr.getMonth() == month && tr.getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactions(Account account) {
        int id = account.getId();
        return getTransactions().stream()
                .filter(tr -> tr.getAccountDebitedId() == id || tr.getAccountCreditedId() == id)
                .collect(Collectors.toList());
    }

    public List<Transaction> getTransactionsByCategories(Collection<Category> categories) {
        var ids = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        return getTransactions().stream()
                .filter(tr -> ids.contains(tr.getAccountDebitedCategoryId())
                        || ids.contains(tr.getAccountCreditedCategoryId()))
                .collect(Collectors.toList());
    }

    public Set<String> getUniqueTransactionComments() {
        return getTransactions().stream()
                .map(Transaction::getComment)
                .filter(c -> !c.isEmpty())
                .collect(Collectors.toSet());
    }

    public long getTransactionCount(Account account) {
        int id = account.getId();

        return getTransactions().stream()
                .filter(tr -> tr.getAccountDebitedId() == id || tr.getAccountCreditedId() == id)
                .count();
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

    private <T extends Record> void loadDto(Class<? extends Dto<T>> dtoClass, Map<Integer, T> result) {
        List<? extends Dto<T>> dtoList = getAll(dtoClass);
        result.clear();
        for (Dto<T> dto : dtoList) {
            T model = dto.decrypt(encryptionKey);
            result.put(model.getId(), model);
        }
    }


    public void preload(Consumer<String> progress) {
        synchronized (preloadingProperty) {
            preloadingProperty.set(true);

            progress.accept("Preloading primary keys... ");
            preload(TABLE_CLASSES);
            progress.accept(" done\n");

            progress.accept("Preloading data...\n");

            progress.accept("    categories... ");
            loadDto(CategoryDto.class, categoriesMap);
            progress.accept("done\n");

            progress.accept("    contacts... ");
            loadDto(ContactDto.class, contactsMap);
            progress.accept("done\n");

            progress.accept("    currencies... ");
            loadDto(CurrencyDto.class, currencyMap);
            progress.accept("done\n");

            progress.accept("    accounts... ");
            loadDto(AccountDto.class, accountsMap);
            progress.accept("done\n");

            progress.accept("    transaction groups... ");
            loadDto(TransactionGroupDto.class, transactionGroupsMap);
            progress.accept("done\n");

            progress.accept("    transactions... ");
            loadDto(TransactionDto.class, transactionsMap);
            progress.accept("done\n");

            progress.accept("done\n");
            preloadingProperty.set(false);
        }
    }

    private void deleteAll(Connection conn, List<Class<? extends Record>> tables) {
        tables.forEach(t -> {
            deleteAll(conn, t);
            resetPrimaryKey(t);
        });
    }

    public void initialize(DataSource ds) {
        synchronized (preloadingProperty) {
            setDataSource(ds);

            preloadingProperty.set(true);
            categoriesMap.clear();
            contactsMap.clear();
            currencyMap.clear();
            accountsMap.clear();
            transactionGroupsMap.clear();
            transactionsMap.clear();
            preloadingProperty.set(false);
        }
    }

    public boolean isOpen() {
        return getDataSource() != null;
    }

    public void importFullDump(Import imp, Consumer<String> progress) {
        try (var conn = getDataSource().getConnection()) {
            progress.accept("Truncating tables... ");
            deleteAll(conn, TABLE_CLASSES_REVERSED);
            progress.accept(" done\n");

            progress.accept("Importing data...\n");

            progress.accept("    categories... ");
            insert(conn, BATCH_SIZE, imp.getCategories().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("    currencies... ");
            insert(conn, BATCH_SIZE, imp.getCurrencies().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("    accounts... ");
            insert(conn, BATCH_SIZE, imp.getAccounts().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("    contacts... ");
            insert(conn, BATCH_SIZE, imp.getContacts().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("    transaction groups... ");
            insert(conn, BATCH_SIZE, imp.getTransactionGroups().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("    transactions... ");
            insert(conn, BATCH_SIZE, imp.getTransactions().stream()
                    .map(c -> newDto(c, encryptionKey)).collect(Collectors.toList()));
            progress.accept("done\n");

            progress.accept("done\n");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }


    private Optional<? extends MoneyRecord> findByGuid(Collection<? extends MoneyRecord> records, String guid) {
        return records.stream().filter(r -> r.getGuid().equals(guid)).findFirst();
    }

    private void mapImportedIds(Map<Integer, Pair<Integer, ImportAction>> idMap,
                                Collection<? extends MoneyRecord> existing,
                                List<? extends MoneyRecord> toImport) {
        toImport.forEach(it -> {
            var found = findByGuid(existing, it.getGuid()).orElse(null);
            if (found != null) {
                if (it.getModified() > found.getModified()) {
                    idMap.put(it.getId(), new Pair<>(found.getId(), ImportAction.UPDATE));
                } else {
                    idMap.put(it.getId(), new Pair<>(found.getId(), ImportAction.IGNORE));
                }
            } else {
                idMap.put(it.getId(), new Pair<>(generatePrimaryKey(dtoClass(it)), ImportAction.INSERT));
            }
        });
    }

    private <T extends MoneyRecord> void importTable(Connection conn,
                                                     List<? extends T> toImport,
                                                     Map<Integer, Pair<Integer, ImportAction>> idMap,
                                                     Function<T, T> replacement) {
        toImport.forEach(it -> {
            var action = idMap.get(it.getId()).getValue();

            if (action != ImportAction.IGNORE) {
                var replaced = replacement.apply(it);

                if (action == ImportAction.INSERT) {
                    insert(conn, newDto(replaced, encryptionKey));
                } else {
                    update(conn, newDto(replaced, encryptionKey));
                }
            }
        });
    }

    private static int getMappedId(Map<Integer, Pair<Integer, ImportAction>> map, int id) {
        var mapped = map.get(id);
        return mapped != null ? mapped.getKey() : id;
    }

    public void importRecords(Import imp, Consumer<String> progress) {
        var categoryIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();
        var currencyIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();
        var contactIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();
        var accountIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();
        var transactionGroupIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();
        var transactionIdMap = new HashMap<Integer, Pair<Integer, ImportAction>>();

        mapImportedIds(currencyIdMap, currencyMap.values(), imp.getCurrencies());
        mapImportedIds(categoryIdMap, categoriesMap.values(), imp.getCategories());
        mapImportedIds(contactIdMap, contactsMap.values(), imp.getContacts());
        mapImportedIds(accountIdMap, accountsMap.values(), imp.getAccounts());
        mapImportedIds(transactionGroupIdMap, transactionGroupsMap.values(), imp.getTransactionGroups());
        mapImportedIds(transactionIdMap, transactionsMap.values(), imp.getTransactions());

        try (Connection conn = getDataSource().getConnection()) {
            try {
                conn.setAutoCommit(false);

                importTable(conn, imp.getCategories(), categoryIdMap,
                        it -> it.copy(getMappedId(categoryIdMap, it.getId())));

                importTable(conn, imp.getCurrencies(), currencyIdMap,
                        it -> it.copy(getMappedId(currencyIdMap, it.getId())));

                importTable(conn, imp.getContacts(), contactIdMap,
                        it -> it.copy(getMappedId(contactIdMap, it.getId())));

                importTable(conn, imp.getAccounts(), accountIdMap,
                        it -> it.copy(getMappedId(accountIdMap, it.getId()),
                                getMappedId(categoryIdMap, it.getCategoryId())));

                importTable(conn, imp.getTransactionGroups(), transactionGroupIdMap,
                        it -> it.copy(getMappedId(transactionGroupIdMap, it.getId())));

                importTable(conn, imp.getTransactions(), transactionIdMap,
                        it -> it.copy(getMappedId(transactionIdMap, it.getId()),
                                getMappedId(accountIdMap, it.getAccountDebitedId()),
                                getMappedId(accountIdMap, it.getAccountCreditedId()),
                                getMappedId(categoryIdMap, it.getAccountDebitedCategoryId()),
                                getMappedId(categoryIdMap, it.getAccountCreditedCategoryId()),
                                getMappedId(contactIdMap, it.getContactId()),
                                getMappedId(transactionGroupIdMap, it.getGroupId())));

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

        try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement()) {
            st.execute("CREATE DATABASE " + schema + " CHARACTER SET = utf8");

            dataSource.setDatabaseName(schema);

            DAO dao = new DAO(dataSource);
            dao.createTables(TABLE_CLASSES);

            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }
}
