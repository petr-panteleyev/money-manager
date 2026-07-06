// Copyright © 2022-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.client;

import org.panteleyev.functional.Either;
import org.panteleyev.money.client.openapi.api.AccountsV1Api;
import org.panteleyev.money.client.openapi.api.CardsV1Api;
import org.panteleyev.money.client.openapi.api.CategoriesV1Api;
import org.panteleyev.money.client.openapi.api.ContactsV1Api;
import org.panteleyev.money.client.openapi.api.CurrenciesV1Api;
import org.panteleyev.money.client.openapi.api.ExchangeSecuritiesV1Api;
import org.panteleyev.money.client.openapi.api.ExchangeSecuritySplitsV1Api;
import org.panteleyev.money.client.openapi.api.IconsV1Api;
import org.panteleyev.money.client.openapi.api.InvestmentDealsV1Api;
import org.panteleyev.money.client.openapi.api.TransactionsV1Api;
import org.panteleyev.money.client.openapi.invoker.ApiException;
import org.panteleyev.money.client.openapi.invoker.Configuration;
import org.panteleyev.money.dto.AccountFlatDTO;
import org.panteleyev.money.dto.CardFlatDTO;
import org.panteleyev.money.dto.CategoryFlatDTO;
import org.panteleyev.money.dto.ContactFlatDTO;
import org.panteleyev.money.dto.CurrencyFlatDTO;
import org.panteleyev.money.dto.ExchangeSecurityFlatDTO;
import org.panteleyev.money.dto.ExchangeSecuritySplitFlatDTO;
import org.panteleyev.money.dto.IconFlatDTO;
import org.panteleyev.money.dto.InvestmentDealFlatDTO;
import org.panteleyev.money.dto.TransactionFlatDTO;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MoneyClient {
    @FunctionalInterface
    private interface ApiCaller<R> {
        R call() throws ApiException;
    }

    private static final String CONTEXT_ROOT = "/money";

    private final AccountsV1Api accountsV1Api;
    private final CardsV1Api cardsV1Api;
    private final CategoriesV1Api categoriesV1Api;
    private final ContactsV1Api contactsV1Api;
    private final CurrenciesV1Api currenciesV1Api;
    private final ExchangeSecuritiesV1Api exchangeSecuritiesV1Api;
    private final ExchangeSecuritySplitsV1Api exchangeSecuritySplitsV1Api;
    private final IconsV1Api iconsV1Api;
    private final InvestmentDealsV1Api investmentDealsV1Api;
    private final TransactionsV1Api transactionsV1Api;

    /**
     * Money client builder.
     */
    public static class Builder {
        private String serverUrl;
        private Duration timeout = Duration.ofMillis(1000);

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
         * Defines HTTP connect timeout.
         *
         * @param timeout timeout
         * @return this
         */
        public Builder withConnectTimeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public MoneyClient build() {
            Objects.requireNonNull(serverUrl, "Server URL cannot be null");
            return new MoneyClient(serverUrl, timeout);
        }
    }

    private MoneyClient(String serverUrl, Duration connectTimeout) {
        var defaultClient = Configuration.getDefaultApiClient();
        defaultClient.updateBaseUri(serverUrl);
        defaultClient.setBasePath(CONTEXT_ROOT);
        defaultClient.setConnectTimeout(connectTimeout);

        accountsV1Api = new AccountsV1Api(defaultClient);
        cardsV1Api = new CardsV1Api(defaultClient);
        categoriesV1Api = new CategoriesV1Api(defaultClient);
        contactsV1Api = new ContactsV1Api(defaultClient);
        currenciesV1Api = new CurrenciesV1Api(defaultClient);
        exchangeSecuritiesV1Api = new ExchangeSecuritiesV1Api(defaultClient);
        exchangeSecuritySplitsV1Api = new ExchangeSecuritySplitsV1Api(defaultClient);
        iconsV1Api = new IconsV1Api(defaultClient);
        investmentDealsV1Api = new InvestmentDealsV1Api(defaultClient);
        transactionsV1Api = new TransactionsV1Api(defaultClient);
    }


    /* Accounts */
    public Either<ClientError, List<AccountFlatDTO>> getAccounts() {
        return call(accountsV1Api::getAccounts);
    }

    public Either<ClientError, AccountFlatDTO> getAccount(UUID uuid) {
        return call(() -> accountsV1Api.getAccountByUuid(uuid));
    }

    public Either<ClientError, AccountFlatDTO> putAccount(AccountFlatDTO dto) {
        return call(() -> accountsV1Api.putAccount(dto));
    }

    /* Cards */
    public Either<ClientError, List<CardFlatDTO>> getCards() {
        return call(cardsV1Api::getCards);
    }

    public Either<ClientError, CardFlatDTO> getCard(UUID uuid) {
        return call(() -> cardsV1Api.getCardByUuid(uuid));
    }

    public Either<ClientError, CardFlatDTO> putCard(CardFlatDTO dto) {
        return call(() -> cardsV1Api.putCard(dto));
    }

    /* Categories */
    public Either<ClientError, List<CategoryFlatDTO>> getCategories() {
        return call(categoriesV1Api::getCategories);
    }

    public Either<ClientError, CategoryFlatDTO> getCategory(UUID uuid) {
        return call(() -> categoriesV1Api.getCategoryByUuid(uuid));
    }

    public Either<ClientError, CategoryFlatDTO> putCategory(CategoryFlatDTO dto) {
        return call(() -> categoriesV1Api.putCategory(dto));
    }

    /* Contacts */
    public Either<ClientError, List<ContactFlatDTO>> getContacts() {
        return call(contactsV1Api::getContacts);
    }

    public Either<ClientError, ContactFlatDTO> getContact(UUID uuid) {
        return call(() -> contactsV1Api.getContactByUuid(uuid));
    }

    public Either<ClientError, ContactFlatDTO> putContact(ContactFlatDTO dto) {
        return call(() -> contactsV1Api.putContact(dto));
    }

    /* Currencies */
    public Either<ClientError, List<CurrencyFlatDTO>> getCurrencies() {
        return call(currenciesV1Api::getCurrencies);
    }

    public Either<ClientError, CurrencyFlatDTO> getCurrency(UUID uuid) {
        return call(() -> currenciesV1Api.getCurrencyByUuid(uuid));
    }

    public Either<ClientError, CurrencyFlatDTO> putCurrency(CurrencyFlatDTO dto) {
        return call(() -> currenciesV1Api.putCurrency(dto));
    }

    /* Exchange Securities */
    public Either<ClientError, List<ExchangeSecurityFlatDTO>> getExchangeSecurities() {
        return call(exchangeSecuritiesV1Api::getExchangeSecurities);
    }

    public Either<ClientError, ExchangeSecurityFlatDTO> getExchangeSecurity(UUID uuid) {
        return call(() -> exchangeSecuritiesV1Api.getExchangeSecurityByUuid(uuid));
    }

    public Either<ClientError, ExchangeSecurityFlatDTO> putExchangeSecurity(ExchangeSecurityFlatDTO dto) {
        return call(() -> exchangeSecuritiesV1Api.putExchangeSecurity(dto));
    }

    /* Exchange Security Splits */
    public Either<ClientError, List<ExchangeSecuritySplitFlatDTO>> getExchangeSecuritySplits() {
        return call(exchangeSecuritySplitsV1Api::getExchangeSecuritySplits);
    }

    public Either<ClientError, ExchangeSecuritySplitFlatDTO> getExchangeSecuritySplit(UUID uuid) {
        return call(() -> exchangeSecuritySplitsV1Api.getExchangeSecuritySplitByUuid(uuid));
    }

    public Either<ClientError, ExchangeSecuritySplitFlatDTO> putExchangeSecuritySplit(
            ExchangeSecuritySplitFlatDTO dto)
    {
        return call(() -> exchangeSecuritySplitsV1Api.putExchangeSecuritySplit(dto));
    }

    /* Icons */
    public Either<ClientError, List<IconFlatDTO>> getIcons() {
        return call(iconsV1Api::getIcons);
    }

    public Either<ClientError, IconFlatDTO> getIcon(UUID uuid) {
        return call(() -> iconsV1Api.getIconByUuid(uuid));
    }

    public Either<ClientError, IconFlatDTO> putIcon(IconFlatDTO dto) {
        return call(() -> iconsV1Api.putIcon(dto));
    }

    /* Investment Deals */
    public Either<ClientError, List<InvestmentDealFlatDTO>> getInvestmentDeals() {
        return call(investmentDealsV1Api::getInvestmentDeals);
    }

    public Either<ClientError, InvestmentDealFlatDTO> getInvestmentDeal(UUID uuid) {
        return call(() -> investmentDealsV1Api.getInvestmentDealByUuid(uuid));
    }

    public Either<ClientError, InvestmentDealFlatDTO> putInvestmentDeal(InvestmentDealFlatDTO dto) {
        return call(() -> investmentDealsV1Api.putInvestmentDeal(dto));
    }

    /* Transactions */
    public Either<ClientError, List<TransactionFlatDTO>> getTransactions() {
        return call(transactionsV1Api::getTransactions);
    }

    public Either<ClientError, TransactionFlatDTO> getTransaction(UUID uuid) {
        return call(() -> transactionsV1Api.getTransactionByUuid(uuid));
    }

    public Either<ClientError, TransactionFlatDTO> putTransaction(TransactionFlatDTO dto) {
        return call(() -> transactionsV1Api.putTransaction(dto));
    }

    //

    private static <R> Either<ClientError, R> call(ApiCaller<R> caller) {
        try {
            return Either.right(caller.call());
        } catch (ApiException ex) {
            return Either.left(new ClientError(ex.getCode(), ex.getMessage()));
        }
    }
}

