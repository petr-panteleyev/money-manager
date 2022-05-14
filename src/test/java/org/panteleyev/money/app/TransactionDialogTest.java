/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.ButtonType;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.statements.StatementRecord;
import org.panteleyev.money.test.BaseTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import static org.panteleyev.money.app.Bundles.translate;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TransactionDialogTest extends BaseTest {
    private static final BigDecimal RATE = BigDecimal.ONE.setScale(6, RoundingMode.HALF_UP);

    private final Category category;
    private final Contact contact;
    private final Account account1;
    private final Account account2;
    private final Account account3;

    private final DataCache cache;

    public TransactionDialogTest() {
        var currency1 = new Currency.Builder()
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

        var currency2 = new Currency.Builder()
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

        category = new Category.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        contact = new Contact.Builder()
            .uuid(UUID.randomUUID())
            .name(UUID.randomUUID().toString())
            .build();

        account1 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .categoryUuid(category.uuid())
            .currencyUuid(currency1.uuid())
            .enabled(true)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // different currency
        account2 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .categoryUuid(category.uuid())
            .currencyUuid(currency2.uuid())
            .enabled(true)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // same currency as (1)
        account3 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .type(CategoryType.BANKS_AND_CASH)
            .categoryUuid(category.uuid())
            .currencyUuid(currency1.uuid())
            .enabled(true)
            .uuid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        cache = new DataCache() {
            {
                getCurrencies().addAll(currency1, currency2);
                getCategories().add(category);
                getContacts().add(contact);
                getAccounts().addAll(account1, account2, account3);
            }
        };
    }

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private TransactionDialog createDialog() {
        var dialog = new TransactionDialog(null, null, cache);
        callPrivateMethod(dialog, "onChangedTransactionTypes");
        callPrivateMethod(dialog, "setupContactMenu");
        callPrivateMethod(dialog, "setupAccountMenus");
        callPrivateMethod(dialog, "setupComments");
        return dialog;
    }

    private TransactionDialog createDialog(Transaction transaction) {
        var dialog = new TransactionDialog(null, null, transaction, cache);
        callPrivateMethod(dialog, "onChangedTransactionTypes");
        callPrivateMethod(dialog, "setupContactMenu");
        callPrivateMethod(dialog, "setupAccountMenus");
        callPrivateMethod(dialog, "setupComments");
        return dialog;
    }

    private TransactionDialog createDialog(StatementRecord record) {
        var dialog = new TransactionDialog(null, null, record, null, cache);
        callPrivateMethod(dialog, "onChangedTransactionTypes");
        callPrivateMethod(dialog, "setupContactMenu");
        callPrivateMethod(dialog, "setupAccountMenus");
        callPrivateMethod(dialog, "setupComments");
        return dialog;
    }

    private void setUserInput(TransactionDialog dialog, Transaction t, String contactName) {
        // Transaction type
        dialog.getTypeEdit().setText(translate(t.type()));

        // Debited account
        dialog.getDebitedAccountEdit().setText(cache.getAccount(t.accountDebitedUuid())
            .map(Account::name)
            .orElse(""));

        // Credited account
        dialog.getCreditedAccountEdit().setText(cache.getAccount(t.accountCreditedUuid())
            .map(Account::name)
            .orElse(""));

        dialog.getCommentEdit().setText(t.comment());
        dialog.getSumEdit().setText(t.amount().toString());
        dialog.getCheckedCheckBox().setSelected(t.checked());

        var uuid = t.contactUuid();
        if (uuid != null) {
            dialog.getContactEdit().setText(cache.getContact(uuid).map(Contact::name).orElse(""));
        }

        if (contactName != null) {
            dialog.getContactEdit().setText(contactName);
        }
    }

    private void callPrivateMethod(Object object, String name) {
        try {
            var method = object.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            method.invoke(object);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void pressOkButton(TransactionDialog dialog) {
        dialog.getDialogPane()
            .lookupButton(ButtonType.OK)
            .fireEvent(new ActionEvent());
    }

    private Transaction createTestTransaction(Account debit, Account credit, Contact contact) {
        var now = LocalDate.now();
        var builder = new Transaction.Builder()
            .uuid(UUID.randomUUID())
            .type(TransactionType.CARD_PAYMENT)
            .accountCreditedType(category.type())
            .accountDebitedType(category.type())
            .accountCreditedCategoryUuid(category.uuid())
            .accountDebitedCategoryUuid(category.uuid())
            .accountDebitedUuid(debit.uuid())
            .accountCreditedUuid(credit.uuid())
            .day(now.getDayOfMonth())
            .month(now.getMonthValue())
            .year(now.getYear())
            .comment(UUID.randomUUID().toString())
            .amount(BigDecimal.valueOf(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP))
            .checked(RANDOM.nextBoolean());

        if (Objects.equals(debit.currencyUuid(), credit.currencyUuid())) {
            builder.rate(BigDecimal.ONE);
        } else {
            builder.rate(BigDecimal.valueOf(RANDOM.nextDouble()));
        }

        if (contact != null) {
            builder.contactUuid(contact.uuid());
        }

        return builder.build();
    }

    private StatementRecord createTestStatementRecord() {
        var builder = new StatementRecord.Builder()
            .actual(LocalDate.now())
            .amount(BigDecimal.valueOf(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP).toString());
        return builder.build();
    }

    private void assertMainFields(Transaction r, Transaction t) {
        assertNotNull(r.uuid(), "UUID must not be null");

        assertEquals(r.type(), t.type(),
            "Transaction type ID is invalid");

        // Debited account
        assertEquals(r.accountDebitedUuid(), t.accountDebitedUuid(),
            "Debited account UUID is invalid");
        assertEquals(r.accountDebitedCategoryUuid(), t.accountDebitedCategoryUuid(),
            "Debited account category UUID is invalid");
        assertEquals(r.accountDebitedType(), t.accountDebitedType(),
            "Debited account category type ID is invalid");

        // Credited account
        assertEquals(r.accountCreditedUuid(), t.accountCreditedUuid(),
            "Credited account UUID is invalid");
        assertEquals(r.accountCreditedCategoryUuid(), t.accountCreditedCategoryUuid(),
            "Credited account category UUID is invalid");
        assertEquals(r.accountCreditedType(), t.accountCreditedType(),
            "Credited account category type ID is invalid");

        assertEquals(r.day(), t.day(), "Day is invalid");
        assertEquals(r.month(), t.month(), "Month is invalid");
        assertEquals(r.year(), t.year(), "Year is invalid");
        assertEquals(r.comment(), t.comment(), "Comment is invalid");
        assertEquals(r.checked(), t.checked(), "Checked status is invalid");
    }

    private void assertStatementRecord(TransactionDialog pane, StatementRecord record) {
        assertEquals(pane.getSumEdit().getText(), record.getAmount());
        assertEquals(pane.getDatePicker().getValue(), record.getActual());
    }

    private void failOnEmptyBuilder() {
        Assert.fail("Builder is null");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        var transaction = createTestTransaction(account1, account3, null);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var dialog = createDialog();
            setUserInput(dialog, transaction, null);
            pressOkButton(dialog);
            var builder = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(Optional.ofNullable(builder));
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            assertNull(builder.getUuid());
            assertNull(builder.getNewContactName());
            var resultedTransaction = builder
                .build();
            assertMainFields(resultedTransaction, transaction);
            assertEquals(resultedTransaction.contactUuid(), transaction.contactUuid(),
                "Contact UUID is invalid");
            assertEquals(resultedTransaction.rate(), RATE, "Rate is invalid");
            assertEquals(resultedTransaction.amount(), transaction.amount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        var transaction = createTestTransaction(account1, account3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var dialog = createDialog();
            setUserInput(dialog, transaction, null);
            pressOkButton(dialog);
            var builder = dialog.getResultConverter().call(ButtonType.OK);
            queue.add(Optional.ofNullable(builder));
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            assertNull(builder.getUuid());
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            assertEquals(resultedTransaction.contactUuid(), transaction.contactUuid(),
                "Contact UUID is invalid");
            assertEquals(resultedTransaction.rate(), RATE, "Rate is invalid");
            assertEquals(resultedTransaction.amount(), transaction.amount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        var transaction = createTestTransaction(account1, account3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        var newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            var dialog = createDialog();
            setUserInput(dialog, transaction, newContact);
            pressOkButton(dialog);
            var builder = dialog.getResultConverter().call(ButtonType.OK);
            assertTrue(dialog.getRate1Edit().isDisabled());
            queue.add(Optional.ofNullable(builder));
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            assertNull(builder.getUuid());
            assertEquals(builder.getNewContactName(), newContact);
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            assertNull(resultedTransaction.contactUuid(), "Contact UUID is invalid");
            assertEquals(resultedTransaction.rate(), RATE, "Rate is invalid");
            assertEquals(resultedTransaction.amount(), transaction.amount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        var transaction = createTestTransaction(account1, account3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var dialog = createDialog(transaction);
            pressOkButton(dialog);
            var builder = dialog.getResultConverter().call(ButtonType.OK);
            assertTrue(dialog.getRate1Edit().isDisabled());
            queue.add(Optional.ofNullable(builder));
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            assertNull(builder.getNewContactName());

            var resultedTransaction = builder.build();
            assertEquals(resultedTransaction.uuid(), transaction.uuid());

            assertMainFields(resultedTransaction, transaction);
            assertEquals(resultedTransaction.contactUuid(), transaction.contactUuid(),
                "Contact UUID is invalid");
            assertEquals(resultedTransaction.rate(), transaction.rate(), "Rate is invalid");
            assertEquals(resultedTransaction.amount(), transaction.amount(), "Amount is invalid");

        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        var transaction = createTestTransaction(account1, account2, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var dialog = createDialog(transaction);
            pressOkButton(dialog);
            var builder = dialog.getResultConverter().call(ButtonType.OK);
            Assert.assertFalse(dialog.getRate1Edit().isDisabled());
            queue.add(Optional.ofNullable(builder));
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            assertNull(builder.getNewContactName());

            var resultedTransaction = builder.build();
            assertEquals(resultedTransaction.uuid(), transaction.uuid(),
                "Transaction UUID is wrong");

            assertMainFields(resultedTransaction, transaction);
            assertEquals(resultedTransaction.contactUuid(), transaction.contactUuid(),
                "Contact UUID is invalid");
            assertEquals(resultedTransaction.rate(), transaction.rate(),
                "Rate is invalid");
            assertEquals(resultedTransaction.amount(), transaction.amount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testStatementRecord() throws Exception {
        var record = createTestStatementRecord();

        var queue = new ArrayBlockingQueue<TransactionDialog>(1);

        Platform.runLater(() -> {
            var dialog = createDialog(record);
            pressOkButton(dialog);
            dialog.getResultConverter().call(ButtonType.OK);
            queue.add(dialog);
        });

        var pane = queue.take();
        assertStatementRecord(pane, record);
    }
}
