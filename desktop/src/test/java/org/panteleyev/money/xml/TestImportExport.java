/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.DocumentType;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.MoneyDocument;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.exchange.ExchangeSecuritySplit;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_DOLLAR;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_EURO;
import static org.panteleyev.money.persistence.BaseDaoTest.ICON_JAVA;
import static org.panteleyev.money.test.BaseTestUtils.newAccount;
import static org.panteleyev.money.test.BaseTestUtils.newCard;
import static org.panteleyev.money.test.BaseTestUtils.newCategory;
import static org.panteleyev.money.test.BaseTestUtils.newContact;
import static org.panteleyev.money.test.BaseTestUtils.newCurrency;
import static org.panteleyev.money.test.BaseTestUtils.newDocument;
import static org.panteleyev.money.test.BaseTestUtils.newExchangeSecurity;
import static org.panteleyev.money.test.BaseTestUtils.newExchangeSecurityShare;
import static org.panteleyev.money.test.BaseTestUtils.newExchangeSecuritySplit;
import static org.panteleyev.money.test.BaseTestUtils.newIcon;
import static org.panteleyev.money.test.BaseTestUtils.newInvestment;
import static org.panteleyev.money.test.BaseTestUtils.newPeriodicPayment;
import static org.panteleyev.money.test.BaseTestUtils.newTransaction;
import static org.panteleyev.money.test.BaseTestUtils.randomString;

/**
 * This test covers XML export/import without database interaction.
 */
public class TestImportExport {
    private static final Icon ICON_1 = newIcon(ICON_DOLLAR);
    private static final Icon ICON_2 = newIcon(ICON_EURO);
    private static final Icon ICON_3 = newIcon(ICON_JAVA);

    private static final MoneyDocument DOCUMENT_1 = newDocument(DocumentType.BILL);
    private static final MoneyDocument DOCUMENT_2 = newDocument(DocumentType.CONTRACT);
    private static final MoneyDocument DOCUMENT_3 = newDocument(DocumentType.RECEIPT);
    private static final MoneyDocument DOCUMENT_4 = newDocument(DocumentType.OTHER);

    private static final Map<UUID, byte[]> BLOBS = Map.of(
            DOCUMENT_1.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_2.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_3.uuid(), randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_4.uuid(), randomString().getBytes(StandardCharsets.UTF_8)
    );

    private static final Category CATEGORY_1 = newCategory(ICON_1);
    private static final Category CATEGORY_2 = newCategory(ICON_1);
    private static final Category CATEGORY_3 = newCategory(ICON_2);

    private static final Currency CURRENCY_1 = newCurrency();
    private static final Currency CURRENCY_2 = newCurrency();
    private static final Currency CURRENCY_3 = newCurrency();

    private static final ExchangeSecurity EXCHANGE_SECURITY_1 = newExchangeSecurity(UUID.randomUUID());
    private static final ExchangeSecurity EXCHANGE_SECURITY_2 = newExchangeSecurityShare(UUID.randomUUID());

    private static final Account ACCOUNT_1 = newAccount(CATEGORY_1, CURRENCY_1);
    private static final Account ACCOUNT_2 = newAccount(CATEGORY_2, CURRENCY_1, ICON_2);
    private static final Account ACCOUNT_3 = newAccount(CATEGORY_3, CURRENCY_2, ICON_3);

    private static final Contact CONTACT_1 = newContact();
    private static final Contact CONTACT_2 = newContact();

    private static final PeriodicPayment PAYMENT_1 = newPeriodicPayment(
            PeriodicPaymentType.CARD_PAYMENT, ACCOUNT_1, ACCOUNT_2, CONTACT_1
    );
    private static final PeriodicPayment PAYMENT_2 = newPeriodicPayment(
            PeriodicPaymentType.MANUAL_PAYMENT, ACCOUNT_2, ACCOUNT_3, CONTACT_2
    );

    private static final Card CARD_1 = newCard(ACCOUNT_1);
    private static final Card CARD_2 = newCard(ACCOUNT_3);

    private static final InvestmentDeal INVESTMENT_DEAL_1 = newInvestment(ACCOUNT_1, EXCHANGE_SECURITY_1, CURRENCY_1);
    private static final InvestmentDeal INVESTMENT_DEAL_2 = newInvestment(ACCOUNT_2, EXCHANGE_SECURITY_2, CURRENCY_2);

    private static final ExchangeSecuritySplit EXCHANGE_SECURITY_SPLIT_1 =
            newExchangeSecuritySplit(EXCHANGE_SECURITY_1);

    private final MoneyDAO dao = new MoneyDAO(new DataCache()) {
        @Override
        public byte[] getDocumentBytes(MoneyDocument document) {
            return BLOBS.get(document.uuid());
        }
    };

    public static List<Arguments> importExportData() {
        return List.of(
                Arguments.of(
                        new DataCache()
                ),
                Arguments.of(
                        new DataCache() {
                            {
                                getIcons().addAll(ICON_1, ICON_2, ICON_3);
                                getDocuments().addAll(DOCUMENT_1, DOCUMENT_2, DOCUMENT_3, DOCUMENT_4);
                                getCategories().addAll(CATEGORY_1, CATEGORY_2);
                                getAccounts().addAll(ACCOUNT_1, ACCOUNT_2, ACCOUNT_3);
                                getContacts().addAll(CONTACT_1, CONTACT_2, newContact("A & B <some@email.com>"));
                                getCurrencies().addAll(CURRENCY_1, CURRENCY_2, CURRENCY_3);
                                getExchangeSecurities().addAll(EXCHANGE_SECURITY_1, EXCHANGE_SECURITY_2);
                                getTransactions().addAll(newTransaction(ACCOUNT_1, ACCOUNT_2),
                                        newTransaction(ACCOUNT_2, ACCOUNT_3),
                                        newTransaction(ACCOUNT_1, ACCOUNT_3),
                                        newTransaction(ACCOUNT_1, ACCOUNT_2));
                                getPeriodicPayments().addAll(PAYMENT_1, PAYMENT_2);
                                getCards().addAll(CARD_1, CARD_2);
                                getInvestmentDeals().addAll(INVESTMENT_DEAL_1, INVESTMENT_DEAL_2);
                                getExchangeSecuritySplits().add(EXCHANGE_SECURITY_SPLIT_1);
                            }
                        }
                )
        );
    }

    @ParameterizedTest
    @MethodSource("importExportData")
    public void testExportAndImport(DataCache cache) throws IOException {
        try (var out = new ByteArrayOutputStream()) {
            new Export(cache, dao).doExport(out, _ -> {});

            var imp = Import.doImport(new ByteArrayInputStream(out.toByteArray()));

            // Assert data
            assertEquals(cache.getIcons(), imp.getIcons());
            assertEquals(cache.getDocuments(), imp.getDocuments());
            assertEquals(cache.getCategories(), imp.getCategories());
            assertEquals(cache.getAccounts(), imp.getAccounts());
            assertEquals(cache.getContacts(), imp.getContacts());
            assertEquals(cache.getCurrencies(), imp.getCurrencies());
            assertEquals(cache.getExchangeSecurities(), imp.getExchangeSecurities());
            assertEquals(cache.getTransactions(), imp.getTransactions());
            assertEquals(cache.getPeriodicPayments(), imp.getPeriodicPayments());
            assertEquals(cache.getCards(), imp.getCards());
            assertEquals(cache.getInvestmentDeals(), imp.getInvestmentDeals());
            assertEquals(cache.getExchangeSecuritySplits(), imp.getExchangeSecuritySplits());

            // Blobs
            var actualBlobs = imp.getBlobs();
            for (var blobContent : imp.getBlobs()) {
                assertArrayEquals(BLOBS.get(blobContent.uuid()), blobContent.bytes());
            }

            assertEquals(actualBlobs.size(), imp.getDocuments().size());
        }
    }
}
