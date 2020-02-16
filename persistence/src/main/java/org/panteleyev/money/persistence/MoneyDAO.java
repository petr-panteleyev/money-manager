package org.panteleyev.money.persistence;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.xml.Import;
import org.panteleyev.mysqlapi.MySqlClient;
import org.panteleyev.mysqlapi.Record;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MoneyDAO {
    private static final MoneyDAO MONEY_DAO = new MoneyDAO();

    private static final DataCache cache = DataCache.cache();

    public static MoneyDAO getDao() {
        return MONEY_DAO;
    }

    public static MySqlClient getClient() {
        return MONEY_DAO.client;
    }

    public static final Comparator<Category> COMPARE_CATEGORY_BY_NAME = Comparator.comparing(Category::getName);
    public static final Comparator<Category> COMPARE_CATEGORY_BY_TYPE =
        (o1, o2) -> o1.getType().getTypeName().compareToIgnoreCase(o2.getType().getTypeName());

    public final static Comparator<Account> COMPARE_ACCOUNT_BY_NAME = Comparator.comparing(Account::getName);
    public final static Comparator<Account> COMPARE_ACCOUNT_BY_CATEGORY = (a1, a2) -> {
        var c1 = cache.getCategory(a1.getCategoryUuid()).map(Category::getName).orElse("");
        var c2 = cache.getCategory(a2.getCategoryUuid()).map(Category::getName).orElse("");
        return c1.compareTo(c2);
    };

    public static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DATE =
        Comparator.comparing(Transaction::getDate).thenComparingLong(Transaction::getCreated);

    public static final Comparator<Transaction> COMPARE_TRANSACTION_BY_DAY =
        Comparator.comparingInt(Transaction::getDay).thenComparingLong(Transaction::getCreated);

    public static final int FIELD_SCALE = 6;

    private static final int BATCH_SIZE = 1000;

    public static final Consumer<String> IGNORE_PROGRESS = x -> { };

    private static final List<Class<? extends Record>> TABLE_CLASSES = List.of(
        Icon.class,
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
        Category.class,
        Icon.class
    );


    private final BooleanProperty preloadingProperty = new SimpleBooleanProperty(false);

    public BooleanProperty preloadingProperty() {
        return preloadingProperty;
    }

    private final MySqlClient client = new MySqlClient();

    ////////////////////////////////////////////////////////////////////////////
    // Icons
    ////////////////////////////////////////////////////////////////////////////

    public void insertIcon(Icon icon) {
        client.insert(icon);
        cache.icons().put(icon.getUuid(), icon);
    }

    public void updateIcon(Icon icon) {
        client.update(icon);
        cache.icons().put(icon.getUuid(), icon);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public void insertCategory(Category category) {
        client.insert(category);
        cache.categories().put(category.getUuid(), category);
    }

    public void updateCategory(Category category) {
        client.update(category);
        cache.categories().put(category.getUuid(), category);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////


    public void insertCurrency(Currency currency) {
        client.insert(currency);
        cache.currencies().put(currency.getUuid(), currency);
    }

    public void updateCurrency(Currency currency) {
        client.update(currency);
        cache.currencies().put(currency.getUuid(), currency);
    }


    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////


    public void insertContact(Contact contact) {
        client.insert(contact);
        cache.contacts().put(contact.getUuid(), contact);
    }

    public void updateContact(Contact contact) {
        client.update(contact);
        cache.contacts().put(contact.getUuid(), contact);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public void insertAccount(Account account) {
        client.insert(account);
        cache.accounts().put(account.getUuid(), account);
    }

    public void updateAccount(Account account) {
        client.update(account);
        cache.accounts().put(account.getUuid(), account);
    }

    public void deleteAccount(Account account) {
        cache.accounts().remove(account.getUuid());
        client.delete(account);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public void insertTransaction(Transaction transaction) {
        client.insert(transaction);
        cache.transactions().put(transaction.getUuid(), transaction);
    }

    public void updateTransaction(Transaction transaction) {
        client.update(transaction);
        cache.transactions().put(transaction.getUuid(), transaction);
    }

    public void deleteTransaction(UUID uuid) {
        client.delete(uuid, Transaction.class);
        cache.transactions().remove(uuid);
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
        synchronized (preloadingProperty) {
            preloadingProperty.set(true);

            progress.accept("Preloading primary keys... ");
            client.preload(TABLE_CLASSES);
            progress.accept(" done\n");

            progress.accept("Preloading data...\n");

            progress.accept("    icons... ");
            client.getAll(Icon.class, cache.iconsMap());
            progress.accept("done\n");

            progress.accept("    categories... ");
            client.getAll(Category.class, cache.categoriesMap());
            progress.accept("done\n");

            progress.accept("    contacts... ");
            client.getAll(Contact.class, cache.contactsMap());
            progress.accept("done\n");

            progress.accept("    currencies... ");
            client.getAll(Currency.class, cache.currencyMap());
            progress.accept("done\n");

            progress.accept("    accounts... ");
            client.getAll(Account.class, cache.accountsMap());
            progress.accept("done\n");

            progress.accept("    transactions... ");
            client.getAll(Transaction.class, cache.transactionsMap());
            progress.accept("done\n");

            progress.accept("done\n");
            preloadingProperty.set(false);
        }
    }

    private void deleteAll(Connection conn, List<Class<? extends Record>> tables) {
        client.truncate(conn, tables);
        tables.forEach(t -> {
//            deleteAll(conn, t);
//            resetPrimaryKey(t);
        });
    }

    public void initialize(DataSource ds) {
        synchronized (preloadingProperty) {
            client.setDataSource(ds);

            preloadingProperty.set(true);
            cache.clear();
            preloadingProperty.set(false);
        }
    }

    public boolean isOpen() {
        return client.getDataSource() != null;
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
                imp.getTransactions().stream().filter(t -> t.getParentUuid().isEmpty()).collect(Collectors.toList()));
            client.insert(conn, BATCH_SIZE,
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
            var found = existing.get(record.getUuid());
            if (found == null) {
                idMap.put(record.getUuid(), ImportAction.INSERT);
            } else {
                if (record.getModified() > found.getModified()) {
                    idMap.put(record.getUuid(), ImportAction.UPDATE);
                } else {
                    idMap.put(record.getUuid(), ImportAction.IGNORE);
                }
            }
        }
    }

    private <T extends MoneyRecord> void importTable(Connection conn,
                                                     List<? extends T> toImport,
                                                     Map<UUID, ImportAction> importActions)
    {
        for (T item : toImport) {
            switch (importActions.get(item.getUuid())) {
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

        calculateActions(iconActions, cache.iconsMap(), imp.getIcons());
        calculateActions(currencyActions, cache.currencyMap(), imp.getCurrencies());
        calculateActions(categoryActions, cache.categoriesMap(), imp.getCategories());
        calculateActions(contactActions, cache.contactsMap(), imp.getContacts());
        calculateActions(accountActions, cache.accountsMap(), imp.getAccounts());
        calculateActions(transactionActions, cache.transactionsMap(), imp.getTransactions());

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
            st.execute("CREATE DATABASE " + schema + " CHARACTER SET = utf8");

            dataSource.setDatabaseName(schema);

            var dao = new MySqlClient(dataSource);
            dao.createTables(TABLE_CLASSES);

            return null;
        } catch (SQLException ex) {
            return ex;
        }
    }
}
