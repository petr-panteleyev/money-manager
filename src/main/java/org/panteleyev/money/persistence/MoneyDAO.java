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
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MoneyDAO {
    private final DataCache cache;
    private final AtomicReference<DataSource> dataSource = new AtomicReference<>();
    // Repositories
    private final CategoryRepository categoryRepository = new CategoryRepository();
    private final AccountRepository accountRepository = new AccountRepository();
    private final ContactRepository contactRepository = new ContactRepository();
    private final CurrencyRepository currencyRepository = new CurrencyRepository();
    private final TransactionRepository transactionRepository = new TransactionRepository();
    private final IconRepository iconRepository = new IconRepository();

    public static final int FIELD_SCALE = 6;

    private static final int BATCH_SIZE = 1000;

    public static final Consumer<String> IGNORE_PROGRESS = x -> { };

    public MoneyDAO(DataCache cache) {
        this.cache = cache;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Generic methods
    ////////////////////////////////////////////////////////////////////////////
    public void withNewConnection(Consumer<Connection> consumer) {
        try (var connection = dataSource.get().getConnection()) {
            try {
                connection.setAutoCommit(false);
                consumer.accept(connection);
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw new RuntimeException(ex);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public <R> R withNewConnection(Function<Connection, R> function) {
        try (var connection = dataSource.get().getConnection()) {
            try {
                connection.setAutoCommit(false);
                R result = function.apply(connection);
                connection.commit();
                return result;
            } catch (SQLException ex) {
                connection.rollback();
                throw new RuntimeException(ex);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Icons
    ////////////////////////////////////////////////////////////////////////////

    public void insertIcon(Icon icon) {
        withNewConnection(conn -> {
            iconRepository.insert(conn, icon);
            cache.add(icon);
        });
    }

    public void updateIcon(Icon icon) {
        withNewConnection(conn -> {
            iconRepository.update(conn, icon);
            cache.update(icon);
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Categories
    ////////////////////////////////////////////////////////////////////////////

    public void insertCategory(Category category) {
        withNewConnection(conn -> {
            categoryRepository.insert(conn, category);
            cache.add(category);
        });
    }

    public void updateCategory(Category category) {
        withNewConnection(conn -> {
            categoryRepository.update(conn, category);
            cache.update(category);
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Currency
    ////////////////////////////////////////////////////////////////////////////

    public void insertCurrency(Currency currency) {
        withNewConnection(conn -> {
            currencyRepository.insert(conn, currency);
            cache.add(currency);
        });
    }

    public void updateCurrency(Currency currency) {
        withNewConnection(conn -> {
            currencyRepository.update(conn, currency);
            cache.update(currency);
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Contacts
    ////////////////////////////////////////////////////////////////////////////

    public void insertContact(Contact contact) {
        withNewConnection(conn -> {
            contactRepository.insert(conn, contact);
            cache.add(contact);
        });
    }

    public void updateContact(Contact contact) {
        withNewConnection(conn -> {
            contactRepository.update(conn, contact);
            cache.update(contact);
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Accounts
    ////////////////////////////////////////////////////////////////////////////

    public void insertAccount(Account account) {
        withNewConnection(conn -> {
            accountRepository.insert(conn, account);
            cache.add(account);
        });
    }

    public void updateAccount(Account account) {
        withNewConnection(conn ->  {
            updateAccount(conn, account);
        });
    }

    public void updateAccount(Connection conn, Account account) {
        accountRepository.update(conn, account);
        cache.update(account);
    }

    public void deleteAccount(Account account) {
        withNewConnection(conn -> {
            cache.remove(account);
            accountRepository.delete(conn, account);
        });
    }

    ////////////////////////////////////////////////////////////////////////////
    // Transactions
    ////////////////////////////////////////////////////////////////////////////

    public void insertTransaction(Transaction transaction) {
        withNewConnection(conn -> {
            transactionRepository.insert(conn, transaction);
            cache.add(transaction);
            updateAccounts(conn, transaction);
        });
    }

    public void updateTransaction(Transaction transaction) {
        withNewConnection(conn -> {
            var oldTransaction = cache.getTransaction(transaction.uuid()).orElseThrow();
            transactionRepository.update(conn, transaction);
            cache.update(transaction);
            updateAccounts(conn, oldTransaction, transaction);
        });
    }

    public void deleteTransaction(Transaction transaction) {
        withNewConnection(conn -> {
            transactionRepository.delete(conn, transaction);
            cache.remove(transaction);
            updateAccounts(conn, transaction);
        });
    }

    /**
     * This method recalculates total values for all involved accounts.
     *
     * @param transactions transactions that were added, updated or deleted.
     */
    private void updateAccounts(Connection conn, Transaction... transactions) {
        Set.of(transactions).stream()
            .map(t -> List.of(
                cache.getAccount(t.accountDebitedUuid()).orElseThrow(),
                cache.getAccount(t.accountCreditedUuid()).orElseThrow())
            )
            .flatMap(Collection::stream)
            .collect(Collectors.toSet())
            .forEach(account -> {
                var total = cache.calculateBalance(account, false, t -> true);
                var waiting = cache.calculateBalance(account, false, t -> !t.checked());
                updateAccount(conn, account.updateBalance(total, waiting));
            });
    }

    public void createTables() {
        withNewConnection(conn -> {
            new LiquibaseUtil(conn).dropAndUpdate();
        });
    }

    public void preload() {
        preload(IGNORE_PROGRESS);
    }

    public void preload(Consumer<String> progress) {
        withNewConnection(conn -> {
            progress.accept("Preloading data...\n");

            progress.accept("    icons... ");
            var iconList = iconRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    categories... ");
            var categoryList = categoryRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    contacts... ");
            var contactList = contactRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    currencies... ");
            var currencyList = currencyRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    accounts... ");
            var accountList = accountRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    transactions... ");
            var transactionList = transactionRepository.getAll(conn);
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
        });
    }

    public void initialize(DataSource ds) {
        dataSource.set(ds);
        cache.clear();
    }

    public void importFullDump(Import imp, Consumer<String> progress) {
        progress.accept("Recreating tables... ");
        createTables();
        progress.accept(" done\n");

        withNewConnection(conn -> {
            progress.accept("Importing data...\n");

            progress.accept("    icons... ");
            iconRepository.insert(conn, BATCH_SIZE, imp.getIcons());
            progress.accept("done\n");

            progress.accept("    categories... ");
            categoryRepository.insert(conn, BATCH_SIZE, imp.getCategories());
            progress.accept("done\n");

            progress.accept("    currencies... ");
            currencyRepository.insert(conn, BATCH_SIZE, imp.getCurrencies());
            progress.accept("done\n");

            progress.accept("    accounts... ");
            accountRepository.insert(conn, BATCH_SIZE, imp.getAccounts());
            progress.accept("done\n");

            progress.accept("    contacts... ");
            contactRepository.insert(conn, BATCH_SIZE, imp.getContacts());
            progress.accept("done\n");

            progress.accept("    transactions... ");
            transactionRepository.insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.parentUuid() == null).toList());
            transactionRepository.insert(conn, BATCH_SIZE,
                imp.getTransactions().stream().filter(t -> t.parentUuid() != null).toList());
            progress.accept("done\n");

            progress.accept("done\n");
        });
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

    private <T extends MoneyRecord> void importTable(Repository<T> repository,
                                                     Connection conn,
                                                     List<? extends T> toImport,
                                                     Map<UUID, ImportAction> importActions)
    {
        for (T item : toImport) {
            switch (importActions.get(item.uuid())) {
                case IGNORE:
                    continue;

                case INSERT:
                    repository.insert(conn, item);
                    break;

                case UPDATE:
                    repository.update(conn, item);
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

        withNewConnection(conn -> {
            importTable(iconRepository, conn, imp.getIcons(), iconActions);
            importTable(categoryRepository, conn, imp.getCategories(), categoryActions);
            importTable(currencyRepository, conn, imp.getCurrencies(), currencyActions);
            importTable(contactRepository, conn, imp.getContacts(), contactActions);
            importTable(accountRepository, conn, imp.getAccounts(), accountActions);
            importTable(transactionRepository, conn, imp.getTransactions(), transactionActions);
        });
    }

    public static Exception resetDatabase(MysqlDataSource dataSource, String schema) {
        try {
            dataSource.setDatabaseName(schema);

            try (var liquibaseConn = dataSource.getConnection()) {
                new LiquibaseUtil(liquibaseConn).dropAndUpdate();
            }

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

        insertTransaction(transaction);
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

        updateTransaction(transaction);
        return transaction;
    }

    private Contact createContact(String name) {
        var contact = new Contact.Builder()
            .uuid(UUID.randomUUID())
            .name(name)
            .build();

        insertContact(contact);
        return contact;
    }

    public LiquibaseUtil.SchemaStatus checkSchemaUpdateStatus() {
        return withNewConnection(conn -> {
            return new LiquibaseUtil(conn).checkSchemaUpdateStatus();
        });
    }

    public void updateSchema() {
        withNewConnection(conn -> {
            new LiquibaseUtil(conn).update();
        });
    }
}
