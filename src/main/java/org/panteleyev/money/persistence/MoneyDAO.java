/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.persistence;

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.application.Platform;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.xml.Import;
import org.panteleyev.mysqlapi.MySqlClient;
import org.panteleyev.mysqlapi.TableRecord;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import static org.panteleyev.money.persistence.DataCache.cache;

public class MoneyDAO {
    private static final MoneyDAO MONEY_DAO = new MoneyDAO();

    private static final DataCache cache = DataCache.cache();

    public static MoneyDAO getDao() {
        return MONEY_DAO;
    }

    public static MySqlClient getClient() {
        return MONEY_DAO.client;
    }

    public static final Comparator<Category> COMPARE_CATEGORY_BY_NAME = Comparator.comparing(Category::name);
    public static final Comparator<Category> COMPARE_CATEGORY_BY_TYPE =
        (o1, o2) -> o1.type().toString().compareToIgnoreCase(o2.type().toString());

    public final static Comparator<Account> COMPARE_ACCOUNT_BY_NAME = Comparator.comparing(Account::name);
    public final static Comparator<Account> COMPARE_ACCOUNT_BY_CATEGORY = (a1, a2) -> {
        var c1 = cache.getCategory(a1.categoryUuid()).map(Category::name).orElse("");
        var c2 = cache.getCategory(a2.categoryUuid()).map(Category::name).orElse("");
        return c1.compareTo(c2);
    };

    public static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DATE =
        Comparator.comparing(Transaction::year)
            .thenComparing(Transaction::month)
            .thenComparing(Transaction::day)
            .thenComparingLong(Transaction::created);

    public static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DAY =
        Comparator.comparingInt(Transaction::day).thenComparingLong(Transaction::created);

    public static final int FIELD_SCALE = 6;

    private static final int BATCH_SIZE = 1000;

    public static final Consumer<String> IGNORE_PROGRESS = x -> { };

    @SuppressWarnings("rawtypes")
    private static final List<Class<? extends TableRecord>> TABLE_CLASSES = List.of(
        Icon.class,
        Category.class,
        Contact.class,
        Currency.class,
        Account.class,
        Transaction.class
    );

    @SuppressWarnings("rawtypes")
    private static final List<Class<? extends TableRecord>> TABLE_CLASSES_REVERSED = List.of(
        Transaction.class,
        Account.class,
        Currency.class,
        Contact.class,
        Category.class,
        Icon.class
    );

    private final MySqlClient client = new MySqlClient();

    ////////////////////////////////////////////////////////////////////////////
    // Icons
    ////////////////////////////////////////////////////////////////////////////

    public void insertIcon(Icon icon) {
        client.insert(icon);
        cache.add(icon);
    }

    public void updateIcon(Icon icon) {
        client.update(icon);
        cache.update(icon);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public void insertCategory(Category category) {
        client.insert(category);
        cache.add(category);
    }

    public void updateCategory(Category category) {
        client.update(category);
        cache.update(category);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public void insertCurrency(Currency currency) {
        client.insert(currency);
        cache.add(currency);
    }

    public void updateCurrency(Currency currency) {
        client.update(currency);
        cache.update(currency);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public void insertContact(Contact contact) {
        client.insert(contact);
        cache.add(contact);
    }

    public void updateContact(Contact contact) {
        client.update(contact);
        cache.update(contact);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public void insertAccount(Account account) {
        client.insert(account);
        cache.add(account);
    }

    public void updateAccount(Account account) {
        client.update(account);
        cache.update(account);
    }

    public void deleteAccount(Account account) {
        cache.remove(account);
        client.delete(account);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public void insertTransaction(Transaction transaction) {
        client.insert(transaction);
        cache.add(transaction);
        updateAccounts(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        var oldTransaction = cache.getTransaction(transaction.uuid()).orElseThrow();
        client.update(transaction);
        cache.update(transaction);
        updateAccounts(oldTransaction, transaction);
    }

    public void deleteTransaction(Transaction transaction) {
        client.delete(transaction.uuid(), Transaction.class);
        cache.remove(transaction);
        updateAccounts(transaction);
    }

    /**
     * This method recalculates total values for all involved accounts.
     *
     * @param transactions transactions that were added, updated or deleted.
     */
    private void updateAccounts(Transaction... transactions) {
        Set.of(transactions).stream()
            .map(t -> List.of(
                cache().getAccount(t.accountDebitedUuid()).orElseThrow(),
                cache().getAccount(t.accountCreditedUuid()).orElseThrow())
            )
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .forEach(account -> {
                var total = cache().calculateBalance(account, false, t -> true);
                var waiting = cache().calculateBalance(account, false, t -> !t.checked());
                updateAccount(account.updateBalance(total, waiting));
            });
    }

    public void createTables(Connection conn) {
        client.createTables(conn, TABLE_CLASSES);
    }

    public void createTables() {
        client.createTables(TABLE_CLASSES);
    }

    public void dropTables() {
        client.dropTables(TABLE_CLASSES_REVERSED);
    }

    public void preload() {
        preload(IGNORE_PROGRESS);
    }

    public void preload(Consumer<String> progress) {
        progress.accept("Preloading primary keys... ");
        client.preload(TABLE_CLASSES);
        progress.accept(" done\n");

        progress.accept("Preloading data...\n");

        progress.accept("    icons... ");
        var iconList = client.getAll(Icon.class);
        progress.accept("done\n");

        progress.accept("    categories... ");
        var categoryList = client.getAll(Category.class);
        progress.accept("done\n");

        progress.accept("    contacts... ");
        var contactList = client.getAll(Contact.class);
        progress.accept("done\n");

        progress.accept("    currencies... ");
        var currencyList = client.getAll(Currency.class);
        progress.accept("done\n");

        progress.accept("    accounts... ");
        var accountList = client.getAll(Account.class);
        progress.accept("done\n");

        progress.accept("    transactions... ");
        var transactionList = client.getAll(Transaction.class);
        progress.accept("done\n");

        progress.accept("done\n");

        CompletableFuture.supplyAsync(() -> {
            cache.getIcons().setAll(iconList);
            cache.getCategories().setAll(categoryList);
            cache.getContacts().setAll(contactList);
            cache.getCurrencies().setAll(currencyList);
            cache.getAccounts().setAll(accountList);
            cache.getTransactions().setAll(transactionList);
            return null;
        }, Platform::runLater);
    }

    public void initialize(DataSource ds) {
        client.setDataSource(ds);
        cache.clear();
    }

    public void importFullDump(Import imp, Consumer<String> progress) {
        try (var conn = client.getDataSource().getConnection()) {
            progress.accept("Recreating tables... ");
            createTables(conn);
            progress.accept(" done\n");

            progress.accept("Importing data...\n");

            progress.accept("    icons... ");
            client.insert(conn, BATCH_SIZE, imp.getIcons());
            progress.accept("done\n");

            progress.accept("    categories... ");
            client.insert(conn, BATCH_SIZE, imp.getCategories());
            progress.accept("done\n");

            progress.accept("    currencies... ");
            client.insert(conn, BATCH_SIZE, imp.getCurrencies());
            progress.accept("done\n");

            progress.accept("    accounts... ");
            client.insert(conn, BATCH_SIZE, imp.getAccounts());
            progress.accept("done\n");

            progress.accept("    contacts... ");
            client.insert(conn, BATCH_SIZE, imp.getContacts());
            progress.accept("done\n");

            progress.accept("    transactions... ");
            client.insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.parentUuid() == null).toList());
            client.insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.parentUuid() != null).toList());
            progress.accept("done\n");

            progress.accept("done\n");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void calculateActions(Map<UUID, ImportAction> idMap,
                                  List<? extends MoneyRecord> existing,
                                  List<? extends MoneyRecord> toImport)
    {
        for (MoneyRecord record : toImport) {
            var found = DataCache.getRecord(existing, record.uuid());
            if (found.isEmpty()) {
                idMap.put(record.uuid(), ImportAction.INSERT);
            } else {
                if (record.modified() > found.get().modified()) {
                    idMap.put(record.uuid(), ImportAction.UPDATE);
                } else {
                    idMap.put(record.uuid(), ImportAction.IGNORE);
                }
            }
        }
    }

    private <T extends MoneyRecord> void importTable(Connection conn,
                                                     List<? extends T> toImport,
                                                     Map<UUID, ImportAction> importActions)
    {
        for (T item : toImport) {
            switch (importActions.get(item.uuid())) {
                case IGNORE:
                    continue;

                case INSERT:
                    client.insert(conn, item);
                    break;

                case UPDATE:
                    client.update(conn, item);
                    break;
            }
        }
    }

    public void importRecords(Import imp, Consumer<String> progress) {
        var iconActions = new HashMap<UUID, ImportAction>();
        var categoryActions = new HashMap<UUID, ImportAction>();
        var currencyActions = new HashMap<UUID, ImportAction>();
        var contactActions = new HashMap<UUID, ImportAction>();
        var accountActions = new HashMap<UUID, ImportAction>();
        var transactionActions = new HashMap<UUID, ImportAction>();

        calculateActions(iconActions, cache.getIcons(), imp.getIcons());
        calculateActions(currencyActions, cache.getCurrencies(), imp.getCurrencies());
        calculateActions(categoryActions, cache.getCategories(), imp.getCategories());
        calculateActions(contactActions, cache.getContacts(), imp.getContacts());
        calculateActions(accountActions, cache.getAccounts(), imp.getAccounts());
        calculateActions(transactionActions, cache.getTransactions(), imp.getTransactions());

        try (var conn = client.getDataSource().getConnection()) {
            try {
                conn.setAutoCommit(false);

                importTable(conn, imp.getIcons(), iconActions);
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
            st.execute("CREATE DATABASE IF NOT EXISTS " + schema + " CHARACTER SET = utf8");

            dataSource.setDatabaseName(schema);

            var dao = new MySqlClient(dataSource);
            dao.createTables(TABLE_CLASSES);

            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }

    public Transaction insertTransaction(Transaction.Builder builder) {
        var newContactName = builder.getNewContactName();
        if (newContactName != null && !newContactName.isEmpty()) {
            var newContact = createContact(newContactName);
            builder.contactUuid(newContact.uuid());
        }

        var transaction = builder
            .modified(0)
            .created(0)
            .build();

        getDao().insertTransaction(transaction);
        return transaction;
    }

    public Transaction updateTransaction(Transaction.Builder builder) {
        var newContactName = builder.getNewContactName();
        if (newContactName != null && !newContactName.isEmpty()) {
            var newContact = createContact(newContactName);
            builder.contactUuid(newContact.uuid());
        }

        var transaction = builder
            .modified(0)
            .build();

        getDao().updateTransaction(transaction);
        return transaction;
    }

    private Contact createContact(String name) {
        var contact = new Contact.Builder()
            .guid(UUID.randomUUID())
            .name(name)
            .build();

        getDao().insertContact(contact);
        return contact;
    }
}
