/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.test.BaseTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AccountDialogTest extends BaseTest {
    private static final String ACCOUNT_NAME = UUID.randomUUID().toString();
    private static final String ACCOUNT_COMMENT = UUID.randomUUID().toString();
    private static final String ACCOUNT_NUMBER = UUID.randomUUID().toString();
    private static final BigDecimal ACCOUNT_INTEREST = BigDecimal.TEN;
    private static final CardType ACCOUNT_CARD_TYPE = CardType.VISA;
    private static final String ACCOUNT_CARD_NUMBER = UUID.randomUUID().toString();
    private static final BigDecimal ACCOUNT_CREDIT = BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP);

    private final Currency curr_1;
    private final Currency curr_2;
    private final Category category;
    private final Account acc_1;
    private final Account acc_2;

    private final DataCache cache;

    public AccountDialogTest() {
        curr_1 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(BigDecimal.valueOf(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        curr_2 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(BigDecimal.valueOf(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        category = new Category.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        acc_1 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .categoryUuid(category.uuid())
            .currencyUuid(curr_1.uuid())
            .enabled(true)
            .openingBalance(randomBigDecimal())
            .accountLimit(randomBigDecimal())
            .closingDate(LocalDate.now())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        acc_2 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .type(CategoryType.PORTFOLIO)
            .categoryUuid(category.uuid())
            .currencyUuid(curr_2.uuid())
            .enabled(true)
            .openingBalance(randomBigDecimal())
            .accountLimit(randomBigDecimal())
            .closingDate(LocalDate.now())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        cache = new DataCache() {
            {
                getCurrencies().addAll(curr_1, curr_2);
                getCategories().add(category);
                getAccounts().add(acc_1);
            }
        };
    }

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private void setUserInput(AccountDialog dialog) {
        dialog.getNameEdit().setText(ACCOUNT_NAME);
        dialog.getCommentEdit().setText(ACCOUNT_COMMENT);
        dialog.getAccountNumberEdit().setText(ACCOUNT_NUMBER);
        dialog.getInterestEdit().setText(ACCOUNT_INTEREST.toString());
        dialog.getCardTypeComboBox().getSelectionModel().select(ACCOUNT_CARD_TYPE);
        dialog.getCardNumberEdit().setText(ACCOUNT_CARD_NUMBER);
        dialog.getCurrencyComboBox().getSelectionModel().select(curr_1);
        dialog.getCreditEdit().setText(ACCOUNT_CREDIT.toString());
    }

    private void setUserInput(AccountDialog dialog, Account account) {
        dialog.getTypeComboBox().getSelectionModel().select(account.type());
        dialog.getNameEdit().setText(account.name());
        dialog.getCommentEdit().setText(account.comment());
        dialog.getAccountNumberEdit().setText(account.accountNumber());
        dialog.getInterestEdit().setText(account.interest().toString());
        dialog.getCardTypeComboBox().getSelectionModel().select(account.cardType());
        dialog.getCardNumberEdit().setText(account.cardNumber());
        dialog.getCurrencyComboBox().getSelectionModel().select(curr_2);
        dialog.getCreditEdit().setText(account.accountLimit().toString());
        dialog.getOpeningBalanceEdit().setText(account.openingBalance().toString());
    }

    @Test
    public void testNewAccount() throws Exception {
        BlockingQueue<Account> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new AccountDialog(null, null, category, cache);
            setUserInput(dialog);
            var account = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(account);
        });

        var account = queue.take();

        assertNotNull(account.uuid());
        assertEquals(account.name(), ACCOUNT_NAME);
        assertEquals(account.comment(), ACCOUNT_COMMENT);
        assertEquals(account.accountNumber(), ACCOUNT_NUMBER);
        assertEquals(account.categoryUuid(), category.uuid());
        assertEquals(account.type(), category.type());
        assertEquals(account.interest().stripTrailingZeros(), ACCOUNT_INTEREST.stripTrailingZeros());
        assertEquals(account.cardType(), ACCOUNT_CARD_TYPE);
        assertEquals(account.cardNumber(), ACCOUNT_CARD_NUMBER);
        assertEquals(account.accountLimit(), ACCOUNT_CREDIT);
        assertTrue(account.enabled());
        assertEquals(account.currencyUuid(), curr_1.uuid());
    }

    @Test
    public void testExistingAccount() throws Exception {
        BlockingQueue<Account> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new AccountDialog(null, null, acc_1, null, cache);
            setUserInput(dialog, acc_2);
            var account = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(account);
        });

        var account = queue.take();

        assertEquals(account.uuid(), acc_1.uuid());
        assertEquals(account.name(), acc_2.name());
        assertEquals(account.comment(), acc_2.comment());
        assertEquals(account.accountNumber(), acc_2.accountNumber());
        assertEquals(account.categoryUuid(), acc_2.categoryUuid());
        assertEquals(account.type(), acc_2.type());
        assertEquals(account.interest().stripTrailingZeros(), acc_2.interest().stripTrailingZeros());
        assertEquals(account.created(), acc_1.created());
        assertEquals(account.currencyUuid(), curr_2.uuid());
        assertEquals(account.closingDate(), acc_2.closingDate());
        assertTrue(account.modified() > acc_1.modified());
        assertEquals(account.openingBalance().stripTrailingZeros(), acc_2.openingBalance().stripTrailingZeros());
        assertEquals(account.accountLimit().stripTrailingZeros(), acc_2.accountLimit().stripTrailingZeros());
        assertTrue(account.enabled());
        assertEquals(account.cardType(), acc_2.cardType());
        assertEquals(account.cardNumber(), acc_2.cardNumber());
    }
}
