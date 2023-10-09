/*
 Copyright © 2019-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.persistence.DataCache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.panteleyev.money.test.BaseTestUtils.randomBigDecimal;

public class AccountDialogTest {
    private static final String ACCOUNT_NAME = UUID.randomUUID().toString();
    private static final String ACCOUNT_COMMENT = UUID.randomUUID().toString();
    private static final String ACCOUNT_NUMBER = UUID.randomUUID().toString();
    private static final BigDecimal ACCOUNT_INTEREST = BigDecimal.TEN;
    private static final BigDecimal ACCOUNT_CREDIT = BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP);

    private final Currency curr_1 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(BigDecimal.valueOf(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

    private final Currency curr_2 = new Currency.Builder()
            .symbol(UUID.randomUUID().toString())
            .description(UUID.randomUUID().toString())
            .formatSymbol(UUID.randomUUID().toString())
            .formatSymbolPosition(1)
            .showFormatSymbol(false)
            .def(false)
            .rate(BigDecimal.valueOf(RANDOM.nextDouble()))
            .direction(1)
            .useThousandSeparator(false)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

    private final Category category = new Category.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

    private final Account acc_1 = new Account.Builder()
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
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

    private final Account acc_2 = new Account.Builder()
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
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

    private final DataCache cache = new DataCache() {
        {
            getCurrencies().addAll(curr_1, curr_2);
            getCategories().add(category);
            getAccounts().add(acc_1);
        }
    };

    @BeforeAll
    public static void init() {
        new JFXPanel();
    }

    private void setUserInput(AccountDialog dialog) {
        dialog.getNameEdit().setText(ACCOUNT_NAME);
        dialog.getCommentEdit().setText(ACCOUNT_COMMENT);
        dialog.getAccountNumberEdit().setText(ACCOUNT_NUMBER);
        dialog.getInterestEdit().setText(ACCOUNT_INTEREST.toString());
        dialog.getCurrencyEdit().setText(curr_1.symbol());
        dialog.getCreditEdit().setText(ACCOUNT_CREDIT.toString());
    }

    private void setUserInput(AccountDialog dialog, Account account) {
        dialog.getTypeComboBox().getSelectionModel().select(account.type());
        dialog.getNameEdit().setText(account.name());
        dialog.getCommentEdit().setText(account.comment());
        dialog.getAccountNumberEdit().setText(account.accountNumber());
        dialog.getInterestEdit().setText(account.interest().toString());
        dialog.getCurrencyEdit().setText(curr_2.symbol());
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
        assertEquals(ACCOUNT_NAME, account.name());
        assertEquals(ACCOUNT_COMMENT, account.comment());
        assertEquals(ACCOUNT_NUMBER, account.accountNumber());
        assertEquals(category.uuid(), account.categoryUuid());
        assertEquals(category.type(), account.type());
        assertEquals(ACCOUNT_INTEREST.stripTrailingZeros(), account.interest().stripTrailingZeros());
        assertEquals(ACCOUNT_CREDIT, account.accountLimit());
        assertTrue(account.enabled());
        assertEquals(curr_1.uuid(), account.currencyUuid());
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

        assertEquals(acc_1.uuid(), account.uuid());
        assertEquals(acc_2.name(), account.name());
        assertEquals(acc_2.comment(), account.comment());
        assertEquals(acc_2.accountNumber(), account.accountNumber());
        assertEquals(acc_2.categoryUuid(), account.categoryUuid());
        assertEquals(acc_2.type(), account.type());
        assertEquals(acc_2.interest().stripTrailingZeros(), account.interest().stripTrailingZeros());
        assertEquals(acc_1.created(), account.created());
        assertEquals(curr_2.uuid(), account.currencyUuid());
        assertEquals(acc_2.closingDate(), account.closingDate());
        assertTrue(account.modified() > acc_1.modified());
        assertEquals(acc_2.openingBalance().stripTrailingZeros(), account.openingBalance().stripTrailingZeros());
        assertEquals(acc_2.accountLimit().stripTrailingZeros(), account.accountLimit().stripTrailingZeros());
        assertTrue(account.enabled());
    }
}
