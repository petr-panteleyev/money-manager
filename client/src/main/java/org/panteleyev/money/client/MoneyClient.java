/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import org.panteleyev.money.client.dto.AccountDto;
import org.panteleyev.money.client.dto.CategoryDto;
import org.panteleyev.money.client.dto.ContactDto;
import org.panteleyev.money.client.dto.CurrencyDto;
import org.panteleyev.money.client.dto.MoneyDto;
import org.panteleyev.money.client.dto.TransactionModificationResponseDto;
import org.panteleyev.money.client.graphql.GQLAccountListResponse;
import org.panteleyev.money.client.graphql.GQLAccountResponse;
import org.panteleyev.money.client.graphql.GQLCategoryListResponse;
import org.panteleyev.money.client.graphql.GQLCategoryResponse;
import org.panteleyev.money.client.graphql.GQLContactListResponse;
import org.panteleyev.money.client.graphql.GQLContactResponse;
import org.panteleyev.money.client.graphql.GQLCurrencyListResponse;
import org.panteleyev.money.client.graphql.GQLCurrencyResponse;
import org.panteleyev.money.client.graphql.GQLListResponse;
import org.panteleyev.money.client.graphql.GQLScalarResponse;
import org.panteleyev.money.client.graphql.GQLTransactionModificationResponse;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.Transaction;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class MoneyClient {
    private static final String CONTEXT_ROOT = "/money";
    private static final String API_ROOT = "/api/1.0.0";

    private static final String API_ICONS = "/icons";
    private static final String API_CURRENCIES = "/currencies";
    private static final String API_CATEGORIES = "/categories";
    private static final String API_CONTACTS = "/contacts";

    private static final String API_ACCOUNTS = "/accounts";
    private static final String API_TRANSACTIONS = "/transactions";
    private static final String API_DOCUMENTS = "/documents";

    private static final String API_GRAPHQL = "/graphql";

    private final Client<Icon> iconClient;
    private final Client<Currency> currencyClient;
    private final Client<Contact> contactClient;
    private final Client<Category> categoryClient;
    private final Client<Account> accountClient;
    private final Client<Transaction> transactionClient;
    private final Client<MoneyDocument> documentClient;
    private final GraphQLClient graphQLClient;

    /**
     * Money client builder.
     */
    public static class Builder {
        private String serverUrl;
        private int timeout = 10;
        private int streamingChunkSize = 1000;

        /**
         * Defines server URL.
         *
         * @param serverUrl server URL
         * @return this
         */
        public Builder withServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        /**
         * Defines HTTP connect timeout in seconds.
         *
         * @param timeout timeout in seconds
         * @return this
         */
        public Builder withConnectTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Defines chunk size for streaming API calls.
         *
         * @param streamingChunkSize chuck size
         * @return this
         */
        public Builder withStreamingChuckSize(int streamingChunkSize) {
            this.streamingChunkSize = streamingChunkSize;
            return this;
        }

        public MoneyClient build() {
            Objects.requireNonNull(serverUrl, "Server URL cannot be null");
            return new MoneyClient(serverUrl, timeout, streamingChunkSize);
        }
    }

    private MoneyClient(String serverUrl, int connectTimeout, int streamingChunkSize) {
        var baseUrl = serverUrl + CONTEXT_ROOT + API_ROOT;
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();

        iconClient = new Client<>(URI.create(baseUrl + API_ICONS),
                httpClient, Icon.class, streamingChunkSize);
        currencyClient = new Client<>(URI.create(baseUrl + API_CURRENCIES),
                httpClient, Currency.class, streamingChunkSize);
        contactClient = new Client<>(URI.create(baseUrl + API_CONTACTS),
                httpClient, Contact.class, streamingChunkSize);
        categoryClient = new Client<>(URI.create(baseUrl + API_CATEGORIES),
                httpClient, Category.class, streamingChunkSize);
        accountClient = new Client<>(URI.create(baseUrl + API_ACCOUNTS),
                httpClient, Account.class, streamingChunkSize);
        transactionClient = new Client<>(URI.create(baseUrl + API_TRANSACTIONS),
                httpClient, Transaction.class, streamingChunkSize);
        documentClient = new Client<>(URI.create(baseUrl + API_DOCUMENTS),
                httpClient, MoneyDocument.class, streamingChunkSize);
        graphQLClient = new GraphQLClient(URI.create(serverUrl + CONTEXT_ROOT + API_GRAPHQL), httpClient);
    }

    /* Icons */
    public List<Icon> getIcons() {
        return iconClient.getAll();
    }

    public void consumeIconStream(Consumer<List<Icon>> listConsumer) {
        iconClient.getAllAsStream(listConsumer);
    }

    public Optional<Icon> getIcon(UUID uuid) {
        return iconClient.get(uuid);
    }

    public Icon putIcon(Icon icon) {
        return iconClient.put(icon);
    }

    /* Currencies */
    public List<Currency> getCurrencies() {
        return currencyClient.getAll();
    }

    public void consumeCurrencyStream(Consumer<List<Currency>> listConsumer) {
        currencyClient.getAllAsStream(listConsumer);
    }

    public Optional<Currency> getCurrency(UUID uuid) {
        return currencyClient.get(uuid);
    }

    public Currency putCurrency(Currency currency) {
        return currencyClient.put(currency);
    }

    /* Contacts */
    public List<Contact> getContacts() {
        return contactClient.getAll();
    }

    public void consumeContactStream(Consumer<List<Contact>> listConsumer) {
        contactClient.getAllAsStream(listConsumer);
    }

    public Optional<Contact> getContact(UUID uuid) {
        return contactClient.get(uuid);
    }

    public Contact putContact(Contact contact) {
        return contactClient.put(contact);
    }

    /* Categories */
    public List<Category> getCategories() {
        return categoryClient.getAll();
    }

    public void consumeCategoryStream(Consumer<List<Category>> listConsumer) {
        categoryClient.getAllAsStream(listConsumer);
    }

    public Optional<Category> getCategory(UUID uuid) {
        return categoryClient.get(uuid);
    }

    public Category putCategory(Category category) {
        return categoryClient.put(category);
    }

    /* Accounts */
    public List<Account> getAccounts() {
        return accountClient.getAll();
    }

    public void consumeAccountStream(Consumer<List<Account>> listConsumer) {
        accountClient.getAllAsStream(listConsumer);
    }

    public Optional<Account> getAccount(UUID uuid) {
        return accountClient.get(uuid);
    }

    public Account putAccount(Account account) {
        return accountClient.put(account);
    }

    /* Transactions */
    public List<Transaction> getTransactions() {
        return transactionClient.getAll();
    }

    public void consumeTransactionStream(Consumer<List<Transaction>> listConsumer) {
        transactionClient.getAllAsStream(listConsumer);
    }

    public Optional<Transaction> getTransaction(UUID uuid) {
        return transactionClient.get(uuid);
    }

    public Transaction putTransaction(Transaction transaction) {
        return transactionClient.put(transaction);
    }

    /* Documents */
    public List<MoneyDocument> getDocuments() {
        return documentClient.getAll();
    }

    public void consumeDocumentStream(Consumer<List<MoneyDocument>> listConsumer) {
        documentClient.getAllAsStream(listConsumer);
    }

    public Optional<MoneyDocument> getDocument(UUID uuid) {
        return documentClient.get(uuid);
    }

    public MoneyDocument putDocument(MoneyDocument document) {
        return documentClient.put(document);
    }

    private <T extends MoneyDto, R extends GQLScalarResponse<T>> GraphQLResponse<T> graphQLQuery(
            String query,
            Class<R> responseClass,
            Map<String, Object> variables
    ) {
        var result = graphQLClient.query(query, responseClass, variables);
        return new GraphQLResponse<T>(
                result.getOperation().orElse(null),
                result.getPayload().orElse(null)
        );
    }

    private <T extends MoneyDto, R extends GQLListResponse<T>> GraphQLListResponse<T> graphQLQueryForList(
            String query,
            Class<R> responseClass,
            Map<String, Object> variables
    ) {
        var result = graphQLClient.query(query, responseClass, variables);
        return new GraphQLListResponse<T>(
                result.getOperation().orElse(null),
                result.getPayload()
        );
    }

    // Category

    public GraphQLResponse<CategoryDto> categoryQuery(String query, Map<String, Object> variables) {
        return graphQLQuery(query, GQLCategoryResponse.class, variables);
    }

    public GraphQLResponse<CategoryDto> categoryQuery(String query) {
        return categoryQuery(query, Map.of());
    }

    public GraphQLListResponse<CategoryDto> categoryListQuery(String query, Map<String, Object> variables) {
        return graphQLQueryForList(query, GQLCategoryListResponse.class, variables);
    }

    public GraphQLListResponse<CategoryDto> categoryListQuery(String query) {
        return categoryListQuery(query, Map.of());
    }

    // Currency

    public GraphQLResponse<CurrencyDto> currencyQuery(String query, Map<String, Object> variables) {
        return graphQLQuery(query, GQLCurrencyResponse.class, variables);
    }

    public GraphQLResponse<CurrencyDto> currencyQuery(String query) {
        return currencyQuery(query, Map.of());
    }

    public GraphQLListResponse<CurrencyDto> currencyListQuery(String query, Map<String, Object> variables) {
        return graphQLQueryForList(query, GQLCurrencyListResponse.class, variables);
    }

    public GraphQLListResponse<CurrencyDto> currencyListQuery(String query) {
        return currencyListQuery(query, Map.of());
    }

    // Contact

    public GraphQLResponse<ContactDto> contactQuery(String query, Map<String, Object> variables) {
        return graphQLQuery(query, GQLContactResponse.class, variables);
    }

    public GraphQLResponse<ContactDto> contactQuery(String query) {
        return contactQuery(query, Map.of());
    }

    public GraphQLListResponse<ContactDto> contactListQuery(String query, Map<String, Object> variables) {
        return graphQLQueryForList(query, GQLContactListResponse.class, variables);
    }

    public GraphQLListResponse<ContactDto> contactListQuery(String query) {
        return contactListQuery(query, Map.of());
    }

    // Contact

    public GraphQLResponse<AccountDto> accountQuery(String query, Map<String, Object> variables) {
        return graphQLQuery(query, GQLAccountResponse.class, variables);
    }

    public GraphQLListResponse<AccountDto> accountListQuery(String query, Map<String, Object> variables) {
        return graphQLQueryForList(query, GQLAccountListResponse.class, variables);
    }

    public GraphQLListResponse<AccountDto> accountListQuery(String query) {
        return accountListQuery(query, Map.of());
    }

    // Transaction

    public GraphQLResponse<TransactionModificationResponseDto> transactionModificationQuery(
            String query,
            Map<String, Object> variables
    ) {
        return graphQLQuery(query, GQLTransactionModificationResponse.class, variables);
    }
}

