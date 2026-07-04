// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.client;

import org.panteleyev.money.client.openapi.dto.AccountFlatDTO;
import org.panteleyev.money.client.openapi.dto.CategoryFlatDTO;
import org.panteleyev.money.client.openapi.dto.ContactFlatDTO;
import org.panteleyev.money.client.openapi.dto.CurrencyFlatDTO;
import org.panteleyev.money.client.openapi.dto.IconFlatDTO;
import org.panteleyev.money.client.openapi.dto.TransactionFlatDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class MoneyClient {
    private static final String CONTEXT_ROOT = "/money";
    private static final String API_ROOT = "/api/v1";

    private static final String API_ICONS = "/icons";
    private static final String API_CURRENCIES = "/currencies";
    private static final String API_CATEGORIES = "/categories";
    private static final String API_CONTACTS = "/contacts";

    private static final String API_ACCOUNTS = "/accounts";
    private static final String API_TRANSACTIONS = "/transactions";

    private final Client<IconFlatDTO> iconClient;
    private final Client<CurrencyFlatDTO> currencyClient;
    private final Client<ContactFlatDTO> contactClient;
    private final Client<CategoryFlatDTO> categoryClient;
    private final Client<AccountFlatDTO> accountClient;
    private final Client<TransactionFlatDTO> transactionClient;

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
                httpClient, IconFlatDTO.class, streamingChunkSize);
        currencyClient = new Client<>(URI.create(baseUrl + API_CURRENCIES),
                httpClient, CurrencyFlatDTO.class, streamingChunkSize);
        contactClient = new Client<>(URI.create(baseUrl + API_CONTACTS),
                httpClient, ContactFlatDTO.class, streamingChunkSize);
        categoryClient = new Client<>(URI.create(baseUrl + API_CATEGORIES),
                httpClient, CategoryFlatDTO.class, streamingChunkSize);
        accountClient = new Client<>(URI.create(baseUrl + API_ACCOUNTS),
                httpClient, AccountFlatDTO.class, streamingChunkSize);
        transactionClient = new Client<>(URI.create(baseUrl + API_TRANSACTIONS),
                httpClient, TransactionFlatDTO.class, streamingChunkSize);
    }

    /* Icons */
    public List<IconFlatDTO> getIcons() {
        return iconClient.getAll();
    }

    public void consumeIconStream(Consumer<List<IconFlatDTO>> listConsumer) {
        iconClient.getAllAsStream(listConsumer);
    }

    public Optional<IconFlatDTO> getIcon(UUID uuid) {
        return iconClient.get(uuid);
    }

    public IconFlatDTO putIcon(IconFlatDTO icon) {
        return iconClient.put(icon);
    }

    /* Currencies */
    public List<CurrencyFlatDTO> getCurrencies() {
        return currencyClient.getAll();
    }

    public void consumeCurrencyStream(Consumer<List<CurrencyFlatDTO>> listConsumer) {
        currencyClient.getAllAsStream(listConsumer);
    }

    public Optional<CurrencyFlatDTO> getCurrency(UUID uuid) {
        return currencyClient.get(uuid);
    }

    public CurrencyFlatDTO putCurrency(CurrencyFlatDTO currency) {
        return currencyClient.put(currency);
    }

    /* Contacts */
    public List<ContactFlatDTO> getContacts() {
        return contactClient.getAll();
    }

    public void consumeContactStream(Consumer<List<ContactFlatDTO>> listConsumer) {
        contactClient.getAllAsStream(listConsumer);
    }

    public Optional<ContactFlatDTO> getContact(UUID uuid) {
        return contactClient.get(uuid);
    }

    public ContactFlatDTO putContact(ContactFlatDTO contact) {
        return contactClient.put(contact);
    }

    /* Categories */
    public List<CategoryFlatDTO> getCategories() {
        return categoryClient.getAll();
    }

    public void consumeCategoryStream(Consumer<List<CategoryFlatDTO>> listConsumer) {
        categoryClient.getAllAsStream(listConsumer);
    }

    public Optional<CategoryFlatDTO> getCategory(UUID uuid) {
        return categoryClient.get(uuid);
    }

    public CategoryFlatDTO putCategory(CategoryFlatDTO category) {
        return categoryClient.put(category);
    }

    /* Accounts */
    public List<AccountFlatDTO> getAccounts() {
        return accountClient.getAll();
    }

    public void consumeAccountStream(Consumer<List<AccountFlatDTO>> listConsumer) {
        accountClient.getAllAsStream(listConsumer);
    }

    public Optional<AccountFlatDTO> getAccount(UUID uuid) {
        return accountClient.get(uuid);
    }

    public AccountFlatDTO putAccount(AccountFlatDTO account) {
        return accountClient.put(account);
    }

    /* Transactions */
    public List<TransactionFlatDTO> getTransactions() {
        return transactionClient.getAll();
    }

    public void consumeTransactionStream(Consumer<List<TransactionFlatDTO>> listConsumer) {
        transactionClient.getAllAsStream(listConsumer);
    }

    public Optional<TransactionFlatDTO> getTransaction(UUID uuid) {
        return transactionClient.get(uuid);
    }

    public TransactionFlatDTO putTransaction(TransactionFlatDTO transaction) {
        return transactionClient.put(transaction);
    }
}

