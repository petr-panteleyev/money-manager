/*
 Copyright © 2017-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import javafx.application.Platform;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.desktop.export.Import;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final ExchangeSecurityRepository exchangeSecurityRepository = new ExchangeSecurityRepository();
    private final CardRepository cardRepository = new CardRepository();
    private final InvestmentDealRepository investmentDealRepository = new InvestmentDealRepository();
    private final ExchangeSecuritySplitRepository exchangeSecuritySplitRepository = new ExchangeSecuritySplitRepository();

    private static final int BATCH_SIZE = 1000;

    public static final Consumer<String> IGNORE_PROGRESS = _ -> {};

    public MoneyDAO(DataCache cache) {
        this.cache = cache;
    }

    //
    //    Generic methods
    //

    public void withNewConnection(Consumer<Connection> consumer) {
        try (var connection = dataSource.get().getConnection()) {
            try {
                connection.setAutoCommit(false);
                consumer.accept(connection);
                connection.commit();
            } catch (Exception ex) {
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
            } catch (Exception ex) {
                connection.rollback();
                throw new RuntimeException(ex);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    //
    // Icons
    //

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

    //
    // Categories
    //

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

    //
    // Currency
    //

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

    public void deleteCurrency(Currency currency) {
        withNewConnection(conn -> {
            currencyRepository.delete(conn, currency);
            cache.remove(currency);
        });
    }

    //
    // Contacts
    //

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

    //
    // Accounts
    //

    public void insertAccount(Account account) {
        withNewConnection(conn -> {
            accountRepository.insert(conn, account);
            cache.add(account);
        });
    }

    public void updateAccount(Account account) {
        withNewConnection(conn -> {
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

    //
    // Transactions
    //

    public void insertTransaction(Transaction transaction) {
        withNewConnection(conn -> {
            transactionRepository.insert(conn, transaction);
            cache.add(transaction);
            updateAccounts(conn, List.of(transaction));
        });
    }

    public void updateTransaction(Transaction transaction) {
        updateTransactions(List.of(transaction));
    }

    public void updateTransactions(Collection<Transaction> transactions) {
        withNewConnection(conn -> {
            var oldAndUpdatedTransactions = new ArrayList<Transaction>(transactions.size() * 2);
            for (var t : transactions) {
                oldAndUpdatedTransactions.add(t);
                oldAndUpdatedTransactions.add(cache.getTransaction(t.uuid()).orElseThrow());
                transactionRepository.update(conn, t);
                cache.update(t);
            }
            updateAccounts(conn, oldAndUpdatedTransactions);
        });
    }

    public void deleteTransaction(Transaction transaction) {
        deleteTransactions(List.of(transaction));
    }

    public void deleteTransactions(Collection<Transaction> transactions) {
        withNewConnection(conn -> {
            for (var t : transactions) {
                transactionRepository.delete(conn, t);
                cache.remove(t);
            }
            updateAccounts(conn, transactions);
        });
    }

    public void checkTransactions(Collection<Transaction> transactions, boolean check) {
        updateTransactions(
                transactions.stream()
                        .filter(t -> t.checked() != check)
                        .map(t -> t.check(check))
                        .toList()
        );
    }

    //
    // Exchange Securities
    //

    public void insertExchangeSecurity(ExchangeSecurity security) {
        withNewConnection(conn -> {
            exchangeSecurityRepository.insert(conn, security);
            cache.add(security);
        });
    }

    public void updateExchangeSecurity(ExchangeSecurity security) {
        withNewConnection(conn -> {
            updateExchangeSecurity(conn, security);
        });
    }

    public void updateExchangeSecurity(Connection conn, ExchangeSecurity security) {
        exchangeSecurityRepository.update(conn, security);
        cache.update(security);
    }

    //
    // Cards
    //

    public void insertCard(Card card) {
        withNewConnection(conn -> {
            cardRepository.insert(conn, card);
            cache.add(card);
        });
    }

    public void updateCard(Card card) {
        withNewConnection(conn -> {
            updateCard(conn, card);
        });
    }

    public void updateCard(Connection conn, Card card) {
        cardRepository.update(conn, card);
        cache.update(card);
    }

    public void deleteCard(Card card) {
        withNewConnection(conn -> {
            cache.remove(card);
            cardRepository.delete(conn, card);
        });
    }

    //
    // Investment deals
    //

    public void insertInvestments(List<InvestmentDeal> investmentDeals) {
        withNewConnection(conn -> {
            investmentDealRepository.insert(conn, BATCH_SIZE, investmentDeals);
            var investmentList = investmentDealRepository.getAll(conn);
            CompletableFuture.supplyAsync(() -> {
                cache.getInvestmentDeals().setAll(investmentList);
                return null;
            }, Platform::runLater);
        });
    }

    //
    // Exchange security splits
    //

    public void insertExchangeSecuritySplit(ExchangeSecuritySplit split) {
        withNewConnection(conn -> {
            exchangeSecuritySplitRepository.insert(conn, split);
            cache.add(split);
        });
    }

    public void updateExchangeSecuritySplit(ExchangeSecuritySplit split) {
        withNewConnection(conn -> {
            exchangeSecuritySplitRepository.update(conn, split);
            cache.update(split);
        });
    }

    public void deleteExchangeSecuritySplit(ExchangeSecuritySplit split) {
        withNewConnection(conn -> {
            cache.remove(split);
            exchangeSecuritySplitRepository.delete(conn, split);
        });
    }

    /**
     * This method recalculates total values for all involved accounts.
     *
     * @param transactions transactions that were added, updated or deleted.
     */
    private void updateAccounts(Connection conn, Collection<Transaction> transactions) {
        var uniqueAccountIds = new HashSet<UUID>();
        for (var t : transactions) {
            uniqueAccountIds.add(t.accountDebitedUuid());
            uniqueAccountIds.add(t.accountCreditedUuid());
        }
        for (var uuid : uniqueAccountIds) {
            cache.getAccount(uuid).ifPresent(account -> {
                var total = cache.calculateBalance(account, false, t -> true);
                var waiting = cache.calculateBalance(account, false, t -> !t.checked());
                updateAccount(conn, account.updateBalance(total, waiting));
            });
        }
    }

    public void createTables() {
        withNewConnection(conn -> {
            new LiquibaseUtil(conn).dropAndUpdate();
        });
    }

    public void preload() {
        preload(Platform::runLater, IGNORE_PROGRESS);
    }

    public void preload(Executor executor, Consumer<String> progress) {
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


            progress.accept("    cards...");
            var cardList = cardRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    transactions... ");
            var transactionList = transactionRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    securities...");
            var exchangeSecuritiesList = exchangeSecurityRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    investments...");
            var investmentList = investmentDealRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("    security splits...");
            var exchangeSecuritySplitsList = exchangeSecuritySplitRepository.getAll(conn);
            progress.accept("done\n");

            progress.accept("done\n");

            CompletableFuture.supplyAsync(() -> {
                cache.getIcons().setAll(iconList);
                cache.getCategories().setAll(categoryList);
                cache.getContacts().setAll(contactList);
                cache.getCurrencies().setAll(currencyList);
                cache.getAccounts().setAll(accountList);
                cache.getCards().setAll(cardList);
                cache.getTransactions().setAll(transactionList);
                cache.getExchangeSecurities().setAll(exchangeSecuritiesList);
                cache.getInvestmentDeals().setAll(investmentList);
                cache.getExchangeSecuritySplits().setAll(exchangeSecuritySplitsList);
                return null;
            }, executor);
        });
    }

    public void initialize(DataSource ds) {
        dataSource.set(ds);
        cache.clear();
    }

    public void importFullDump(Import imp, Consumer<String> progress) {
        progress.accept("Создание таблиц... ");
        createTables();
        progress.accept("выполнено\n");

        withNewConnection(conn -> {
            progress.accept("Импорт данных...\n");

            progress.accept("    значки... ");
            iconRepository.insert(conn, BATCH_SIZE, imp.getIcons());
            progress.accept("выполнено\n");

            progress.accept("    категории... ");
            categoryRepository.insert(conn, BATCH_SIZE, imp.getCategories());
            progress.accept("выполнено\n");

            progress.accept("    валюты... ");
            currencyRepository.insert(conn, BATCH_SIZE, imp.getCurrencies());
            progress.accept("выполнено\n");

            progress.accept("    ценные бумаги... ");
            exchangeSecurityRepository.insert(conn, BATCH_SIZE, imp.getExchangeSecurities());
            progress.accept("выполнено\n");

            progress.accept("    счета... ");
            accountRepository.insert(conn, BATCH_SIZE, imp.getAccounts());
            progress.accept("выполнено\n");

            progress.accept("    карты...");
            cardRepository.insert(conn, BATCH_SIZE, imp.getCards());
            progress.accept("выполнено\n");

            progress.accept("    контакты... ");
            contactRepository.insert(conn, BATCH_SIZE, imp.getContacts());
            progress.accept("выполнено\n");

            progress.accept("    проводки... ");
            transactionRepository.insert(conn, BATCH_SIZE,
                    imp.getTransactions().stream().filter(t -> t.parentUuid() == null).toList());
            transactionRepository.insert(conn, BATCH_SIZE,
                    imp.getTransactions().stream().filter(t -> t.parentUuid() != null).toList());
            progress.accept("выполнено\n");

            progress.accept("    инвестиционные сделки...");
            investmentDealRepository.insert(conn, BATCH_SIZE, imp.getInvestmentDeals());
            progress.accept("выполнено\n");

            progress.accept("    сплиты ценных бумаг...");
            exchangeSecuritySplitRepository.insert(conn, BATCH_SIZE, imp.getExchangeSecuritySplits());
            progress.accept("выполнено\n");

            progress.accept("выполнено\n");
        });
    }

    public static Exception resetDatabase(PGSimpleDataSource dataSource, String schema) {
        try (var liquibaseConn = dataSource.getConnection()) {
            new LiquibaseUtil(liquibaseConn).dropAndUpdate();
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

        var now = System.currentTimeMillis();
        var transaction = builder
                .modified(now)
                .created(now)
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
                .modified(System.currentTimeMillis())
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
