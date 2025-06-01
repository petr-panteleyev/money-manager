/*
 Copyright © 2021-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.MoneyRecord;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplitType;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentDealType;
import org.panteleyev.money.model.investment.InvestmentMarketType;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestRepositories extends BaseDaoTest {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";

    private static final UUID ICON_UUID = UUID.randomUUID();
    private static final UUID CATEGORY_UUID = UUID.randomUUID();
    private static final UUID CURRENCY_UUID = UUID.randomUUID();
    private static final UUID EXCHANGE_SECURITY_UUID = UUID.randomUUID();
    private static final UUID CONTACT_UUID = UUID.randomUUID();
    private static final UUID ACCOUNT_UUID = UUID.randomUUID();
    private static final UUID TRANSACTION_UUID = UUID.randomUUID();
    private static final UUID DOCUMENT_UUID = UUID.randomUUID();
    private static final UUID PERIODIC_PAYMENT_UUID = UUID.randomUUID();
    private static final UUID CARD_UUID = UUID.randomUUID();
    private static final UUID INVESTMENT_UUID = UUID.randomUUID();
    private static final UUID EXCHANGE_SECURITY_SPLIT_UUID = UUID.randomUUID();

    @BeforeAll
    public static void init() {
        var initialized = BaseDaoTest.setupAndSkip();
        assumeTrue(initialized);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        BaseDaoTest.tearDown();
    }

    @Test
    @Order(1)
    public void testIcon() {
        var repository = new IconRepository();
        var insert = BaseTestUtils.newIcon(ICON_UUID, ICON_DOLLAR);
        var update = BaseTestUtils.newIcon(ICON_UUID, ICON_EURO);
        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(2)
    public void testCategory() {
        var repository = new CategoryRepository();

        var insert = new Category(
                CATEGORY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomCategoryType(),
                ICON_UUID,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Category(
                CATEGORY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomCategoryType(),
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(3)
    public void testCurrency() {
        var repository = new CurrencyRepository();

        var insert = new Currency(
                CURRENCY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Currency(
                CURRENCY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(4)
    public void testExchangeSecurity() {
        var repository = new ExchangeSecurityRepository();

        var insert = new ExchangeSecurity(
                EXCHANGE_SECURITY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                LocalDate.now(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                BaseTestUtils.randomInt(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomInt(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new ExchangeSecurity(
                EXCHANGE_SECURITY_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                null,
                null,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                null,
                null,
                null,
                null,
                null,
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(5)
    public void testContact() {
        var repository = new ContactRepository();

        var insert = new Contact(
                CONTACT_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomContactType(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                ICON_UUID,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Contact(
                CONTACT_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomContactType(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(6)
    public void testAccount() {
        var repository = new AccountRepository();

        var insert = new Account(
                ACCOUNT_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomCategoryType(),
                CATEGORY_UUID,
                CURRENCY_UUID,
                EXCHANGE_SECURITY_UUID,
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                ICON_UUID,
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Account(
                ACCOUNT_UUID,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomCategoryType(),
                CATEGORY_UUID,
                null,
                null,
                BaseTestUtils.randomBoolean(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                null,
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(7)
    public void testCard() {
        var repository = new CardRepository();

        var insert = new Card(
                CARD_UUID,
                ACCOUNT_UUID,
                BaseTestUtils.randomCardType(),
                BaseTestUtils.randomString(),
                LocalDate.now(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Card(
                CARD_UUID,
                ACCOUNT_UUID,
                BaseTestUtils.randomCardType(),
                BaseTestUtils.randomString(),
                LocalDate.now(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBoolean(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(8)
    public void testTransaction() {
        var repository = new TransactionRepository();

        var insert = new Transaction(
                TRANSACTION_UUID,
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                BaseTestUtils.randomTransactionType(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBoolean(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                BaseTestUtils.randomCategoryType(),
                BaseTestUtils.randomCategoryType(),
                CATEGORY_UUID,
                CATEGORY_UUID,
                CONTACT_UUID,
                BaseTestUtils.randomString(),
                null,
                BaseTestUtils.randomBoolean(),
                LocalDate.now(),
                CARD_UUID,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new Transaction(
                TRANSACTION_UUID,
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                LocalDate.now(),
                BaseTestUtils.randomTransactionType(),
                BaseTestUtils.randomString(),
                BaseTestUtils.randomBoolean(),
                ACCOUNT_UUID,
                ACCOUNT_UUID,
                BaseTestUtils.randomCategoryType(),
                BaseTestUtils.randomCategoryType(),
                CATEGORY_UUID,
                CATEGORY_UUID,
                null,
                BaseTestUtils.randomString(),
                TRANSACTION_UUID,
                BaseTestUtils.randomBoolean(),
                LocalDate.now(),
                null,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    @Test
    @Order(12)
    public void testInvestment() {
        var repository = new InvestmentDealRepository();

        var insert = new InvestmentDeal(
                INVESTMENT_UUID,
                ACCOUNT_UUID,
                null,
                null,
                BaseTestUtils.randomString(),
                BaseTestUtils.randomLocalDateTime(),
                BaseTestUtils.randomLocalDateTime(),
                InvestmentMarketType.STOCK_MARKET,
                InvestmentOperationType.PURCHASE,
                10,
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomBigDecimal(),
                InvestmentDealType.NORMAL,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        insert(repository, insert);

        var update = new InvestmentDeal(
                insert.uuid(),
                insert.accountUuid(),
                EXCHANGE_SECURITY_UUID,
                CURRENCY_UUID,
                insert.dealNumber(),
                insert.dealDate(),
                insert.accountingDate(),
                InvestmentMarketType.STOCK_MARKET,
                InvestmentOperationType.PURCHASE,
                10,
                insert.price(),
                insert.aci(),
                insert.dealVolume(),
                insert.rate(),
                insert.exchangeFee(),
                insert.brokerFee(),
                insert.amount(),
                InvestmentDealType.NORMAL,
                insert.created(),
                System.currentTimeMillis()
        );

        insert(repository, update);
    }

    @Test
    @Order(13)
    public void testExchangeSecuritySplit() {
        var repository = new ExchangeSecuritySplitRepository();

        var insert = new ExchangeSecuritySplit(
                EXCHANGE_SECURITY_SPLIT_UUID,
                EXCHANGE_SECURITY_UUID,
                ExchangeSecuritySplitType.REVERSE_SPLIT,
                LocalDate.now(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomString(),
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        var update = new ExchangeSecuritySplit(
                insert.uuid(),
                EXCHANGE_SECURITY_UUID,
                ExchangeSecuritySplitType.REVERSE_SPLIT,
                LocalDate.now(),
                BaseTestUtils.randomBigDecimal(),
                BaseTestUtils.randomString(),
                insert.created(),
                System.currentTimeMillis()
        );

        insertAndUpdate(repository, insert, update);
    }

    private static <T extends MoneyRecord> void insert(Repository<T> repository, T insert) {
        dao.withNewConnection(conn -> {
            var uuid = insert.uuid();

            repository.insert(conn, insert);
            assertEquals(insert, repository.get(conn, uuid).orElseThrow());
        });
    }

    private static <T extends MoneyRecord> void insertAndUpdate(Repository<T> repository, T insert, T update) {
        dao.withNewConnection(conn -> {
            var uuid = insert.uuid();

            repository.insert(conn, insert);
            assertEquals(insert, repository.get(conn, uuid).orElseThrow());

            repository.update(conn, update);
            assertEquals(update, repository.get(conn, uuid).orElseThrow());
        });
    }
}
