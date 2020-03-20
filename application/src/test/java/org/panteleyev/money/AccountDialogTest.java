package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

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

    private final DataCache cache;

    public AccountDialogTest() {
        curr_1 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(new BigDecimal(RANDOM.nextDouble()))
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
            .rate(new BigDecimal(RANDOM.nextDouble()))
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

        cache = new DataCache() {
            {
                currencyMap().put(curr_1.uuid(), curr_1);
                currencyMap().put(curr_2.uuid(), curr_2);
                categoriesMap().put(category.uuid(), category);
                accountsMap().put(acc_1.uuid(), acc_1);
            }
        };
    }

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private void setupDialog(AccountDialog dialog) {
        dialog.getNameEdit().setText(ACCOUNT_NAME);
        dialog.getCommentEdit().setText(ACCOUNT_COMMENT);
        dialog.getAccountNumberEdit().setText(ACCOUNT_NUMBER);
        dialog.getInterestEdit().setText(ACCOUNT_INTEREST.toString());
        dialog.getCardTypeComboBox().getSelectionModel().select(ACCOUNT_CARD_TYPE);
        dialog.getCardNumberEdit().setText(ACCOUNT_CARD_NUMBER);
        dialog.getCurrencyComboBox().getSelectionModel().select(curr_1);
        dialog.getCreditEdit().setText(ACCOUNT_CREDIT.toString());
    }

    private void setupDialog(AccountDialog dialog, Account account) {
        dialog.getCurrencyComboBox().getSelectionModel().select(curr_2);
    }

    @Test
    public void testNewAccount() throws Exception {
        BlockingQueue<Account> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new AccountDialog(null, category, cache);
            setupDialog(dialog);
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
            var dialog = new AccountDialog(null, acc_1, null, cache);
            setupDialog(dialog, acc_1);
            var account = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(account);
        });

        var account = queue.take();

        assertEquals(account.uuid(), acc_1.uuid());
        assertEquals(account.name(), acc_1.name());
        assertEquals(account.comment(), acc_1.comment());
        assertEquals(account.accountNumber(), acc_1.accountNumber());
        assertEquals(account.categoryUuid(), category.uuid());
        assertEquals(account.type(), category.type());
        assertEquals(account.interest().stripTrailingZeros(), acc_1.interest().stripTrailingZeros());
        assertEquals(account.created(), acc_1.created());
        assertEquals(account.currencyUuid(), curr_2.uuid());
        assertEquals(account.closingDate(), acc_1.closingDate());
        assertTrue(account.modified() > acc_1.modified());
        assertEquals(account.openingBalance().stripTrailingZeros(), acc_1.openingBalance().stripTrailingZeros());
        assertEquals(account.accountLimit().stripTrailingZeros(), acc_1.accountLimit().stripTrailingZeros());
        assertTrue(account.enabled());
        assertEquals(account.cardType(), acc_1.cardType());
        assertEquals(account.cardNumber(), acc_1.cardNumber());
    }
}
