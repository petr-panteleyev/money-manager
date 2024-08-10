/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.desktop.commons.DocumentProvider;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test covers XML export/import without database interaction.
 */
public class TestImportExport {
    private static final String ICON_DOLLAR = "dollar.png";
    private static final String ICON_EURO = "euro.png";
    private static final String ICON_JAVA = "java.png";

    private static final Icon ICON_1 = BaseTestUtils.newIcon(ICON_DOLLAR);
    private static final Icon ICON_2 = BaseTestUtils.newIcon(ICON_EURO);
    private static final Icon ICON_3 = BaseTestUtils.newIcon(ICON_JAVA);

    private static final MoneyDocument DOCUMENT_1 = BaseTestUtils.newDocument(DocumentType.BILL);
    private static final MoneyDocument DOCUMENT_2 = BaseTestUtils.newDocument(DocumentType.CONTRACT);
    private static final MoneyDocument DOCUMENT_3 = BaseTestUtils.newDocument(DocumentType.RECEIPT);
    private static final MoneyDocument DOCUMENT_4 = BaseTestUtils.newDocument(DocumentType.OTHER);

    private static final Map<UUID, byte[]> BLOBS = Map.of(
            DOCUMENT_1.uuid(), BaseTestUtils.randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_2.uuid(), BaseTestUtils.randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_3.uuid(), BaseTestUtils.randomString().getBytes(StandardCharsets.UTF_8),
            DOCUMENT_4.uuid(), BaseTestUtils.randomString().getBytes(StandardCharsets.UTF_8)
    );

    private static final Category CATEGORY_1 = BaseTestUtils.newCategory(ICON_1);
    private static final Category CATEGORY_2 = BaseTestUtils.newCategory(ICON_1);
    private static final Category CATEGORY_3 = BaseTestUtils.newCategory(ICON_2);

    private static final Currency CURRENCY_1 = BaseTestUtils.newCurrency();
    private static final Currency CURRENCY_2 = BaseTestUtils.newCurrency();
    private static final Currency CURRENCY_3 = BaseTestUtils.newCurrency();

    private static final ExchangeSecurity EXCHANGE_SECURITY_1 = BaseTestUtils.newExchangeSecurity(UUID.randomUUID());
    private static final ExchangeSecurity EXCHANGE_SECURITY_2 = BaseTestUtils.newExchangeSecurityShare(UUID.randomUUID());

    private static final Account ACCOUNT_1 = BaseTestUtils.newAccount(CATEGORY_1, CURRENCY_1);
    private static final Account ACCOUNT_2 = BaseTestUtils.newAccount(CATEGORY_2, CURRENCY_1, ICON_2);
    private static final Account ACCOUNT_3 = BaseTestUtils.newAccount(CATEGORY_3, CURRENCY_2, ICON_3);

    private static final Contact CONTACT_1 = BaseTestUtils.newContact();
    private static final Contact CONTACT_2 = BaseTestUtils.newContact();

    private static final PeriodicPayment PAYMENT_1 = BaseTestUtils.newPeriodicPayment(
            PeriodicPaymentType.CARD_PAYMENT, ACCOUNT_1, ACCOUNT_2, CONTACT_1
    );
    private static final PeriodicPayment PAYMENT_2 = BaseTestUtils.newPeriodicPayment(
            PeriodicPaymentType.MANUAL_PAYMENT, ACCOUNT_2, ACCOUNT_3, CONTACT_2
    );

    private static final Card CARD_1 = BaseTestUtils.newCard(ACCOUNT_1);
    private static final Card CARD_2 = BaseTestUtils.newCard(ACCOUNT_3);

    private static final InvestmentDeal INVESTMENT_DEAL_1 = BaseTestUtils.newInvestment(ACCOUNT_1, EXCHANGE_SECURITY_1, CURRENCY_1);
    private static final InvestmentDeal INVESTMENT_DEAL_2 = BaseTestUtils.newInvestment(ACCOUNT_2, EXCHANGE_SECURITY_2, CURRENCY_2);

    private static final ExchangeSecuritySplit EXCHANGE_SECURITY_SPLIT_1 =
            BaseTestUtils.newExchangeSecuritySplit(EXCHANGE_SECURITY_1);

    private final DocumentProvider documentProvider = document -> BLOBS.get(document.uuid());

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
                                getContacts().addAll(CONTACT_1, CONTACT_2, BaseTestUtils.newContact("A & B <some@email.com>"));
                                getCurrencies().addAll(CURRENCY_1, CURRENCY_2, CURRENCY_3);
                                getExchangeSecurities().addAll(EXCHANGE_SECURITY_1, EXCHANGE_SECURITY_2);
                                getTransactions().addAll(BaseTestUtils.newTransaction(ACCOUNT_1, ACCOUNT_2),
                                        BaseTestUtils.newTransaction(ACCOUNT_2, ACCOUNT_3),
                                        BaseTestUtils.newTransaction(ACCOUNT_1, ACCOUNT_3),
                                        BaseTestUtils.newTransaction(ACCOUNT_1, ACCOUNT_2));
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
            new Export(cache, documentProvider).doExport(out, _ -> {});

            var bytes = out.toByteArray();

            Import.validate(new ByteArrayInputStream(bytes));
            var imp = Import.doImport(new ByteArrayInputStream(bytes));

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
