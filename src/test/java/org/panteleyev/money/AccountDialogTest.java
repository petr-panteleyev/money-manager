/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.persistence.BaseDaoTest;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Currency;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static org.panteleyev.money.BaseTestUtils.RANDOM;
import static org.panteleyev.money.BaseTestUtils.randomBigDecimal;
import static org.panteleyev.money.UiTestUtils.getComboBox;
import static org.panteleyev.money.UiTestUtils.getTextField;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AccountDialogTest extends BaseDaoTest {
    private static final String ACCOUNT_NAME = UUID.randomUUID().toString();
    private static final String ACCOUNT_COMMENT = UUID.randomUUID().toString();
    private static final String ACCOUNT_NUMBER = UUID.randomUUID().toString();
    private static final BigDecimal ACCOUNT_INTEREST = BigDecimal.TEN;

    private Currency curr_1;
    private Currency curr_2;
    private Category category;
    private Account acc_1;

    @BeforeClass
    @Override
    public void setupAndSkip() {
        try {
            super.setupAndSkip();
            getDao().createTables();
            getDao().preload();
            createData();
            new JFXPanel();
        } catch (Exception ex) {
            throw new SkipException("Database not configured");
        }
    }

    @AfterClass
    @Override
    public void cleanup() throws Exception {
        getDao().dropTables();
        super.cleanup();
    }

    private void createData() {
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
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        acc_1 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getGuid())
            .currencyUuid(curr_1.getGuid())
            .enabled(true)
            .openingBalance(randomBigDecimal())
            .accountLimit(randomBigDecimal())
            .closingDate(LocalDate.now())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        getDao().insertCurrency(curr_1);
        getDao().insertCurrency(curr_2);
        getDao().insertCategory(category);
        getDao().insertAccount(acc_1);
    }

    private void setupDialog(AccountDialog dialog) {
        getTextField(dialog, "nameEdit").setText(ACCOUNT_NAME);
        getTextField(dialog, "commentEdit").setText(ACCOUNT_COMMENT);
        getTextField(dialog, "accountNumberEdit").setText(ACCOUNT_NUMBER);
        getTextField(dialog, "interestEdit").setText(ACCOUNT_INTEREST.toString());
        getComboBox(dialog, "currencyComboBox").getSelectionModel().select(curr_1);
    }

    private void setupDialog(AccountDialog dialog, Account account) {
        getComboBox(dialog, "currencyComboBox").getSelectionModel().select(curr_2);
    }

    @Test
    public void testNewAccount() throws Exception {
        BlockingQueue<Account> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new AccountDialog(category);
            setupDialog(dialog);
            var account = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(account);
        });

        var account = queue.take();

        assertNotNull(account.getGuid());
        assertEquals(account.getName(), ACCOUNT_NAME);
        assertEquals(account.getComment(), ACCOUNT_COMMENT);
        assertEquals(account.getAccountNumber(), ACCOUNT_NUMBER);
        assertEquals(account.getCategoryUuid(), category.getGuid());
        assertEquals(account.getTypeId(), category.getType().getId());
        assertEquals(account.getInterest().stripTrailingZeros(), ACCOUNT_INTEREST.stripTrailingZeros());
        assertTrue(account.getEnabled());
        assertEquals(account.getCurrencyUuid().orElse(null), curr_1.getGuid());
    }

    @Test
    public void testExistingAccount() throws Exception {
        BlockingQueue<Account> queue = new ArrayBlockingQueue<>(1);

        Platform.runLater(() -> {
            var dialog = new AccountDialog(acc_1, null);
            setupDialog(dialog, acc_1);
            var account = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(account);
        });

        var account = queue.take();

        assertEquals(account.getGuid(), acc_1.getGuid());
        assertEquals(account.getName(), acc_1.getName());
        assertEquals(account.getComment(), acc_1.getComment());
        assertEquals(account.getAccountNumber(), acc_1.getAccountNumber());
        assertEquals(account.getCategoryUuid(), category.getGuid());
        assertEquals(account.getTypeId(), category.getType().getId());
        assertEquals(account.getInterest().stripTrailingZeros(), acc_1.getInterest().stripTrailingZeros());
        assertEquals(account.getCreated(), acc_1.getCreated());
        assertEquals(account.getCurrencyUuid().orElse(null), curr_2.getGuid());
        assertEquals(account.getClosingDate(), acc_1.getClosingDate());
        assertTrue(account.getModified() > acc_1.getModified());
        assertEquals(account.getOpeningBalance().stripTrailingZeros(), acc_1.getOpeningBalance().stripTrailingZeros());
        assertTrue(account.getEnabled());
    }
}
