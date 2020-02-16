package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;

public class EditorPaneTest extends BaseTest {
    private final Category category;
    private final Contact contact;
    private final Account acc_1;
    private final Account acc_2;
    private final Account acc_3;

    private final DataCache cache;

    public EditorPaneTest() {
        var curr_1 = new Currency.Builder()
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

        var curr_2 = new Currency.Builder()
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
            .catTypeId(CategoryType.BANKS_AND_CASH.getId())
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        contact = new Contact.Builder()
            .name(UUID.randomUUID().toString())
            .build();

        acc_1 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_1.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // different currency
        acc_2 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_2.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        // same currency as (1)
        acc_3 = new Account.Builder()
            .name(UUID.randomUUID().toString())
            .comment(UUID.randomUUID().toString())
            .accountNumber(UUID.randomUUID().toString())
            .typeId(CategoryType.BANKS_AND_CASH.getId())
            .categoryUuid(category.getUuid())
            .currencyUuid(curr_1.getUuid())
            .enabled(true)
            .guid(UUID.randomUUID())
            .modified(System.currentTimeMillis())
            .build();

        cache = new DataCache() {
            {
                currencyMap().putAll(Map.of(curr_1.getUuid(), curr_1, curr_2.getUuid(), curr_2));
                categoriesMap().put(category.getUuid(), category);
                contactsMap().put(contact.getUuid(), contact);
                accountsMap().putAll(Map.of(acc_1.getUuid(), acc_1, acc_2.getUuid(), acc_2, acc_3.getUuid(), acc_3));
            }
        };
    }

    @BeforeClass
    public void setupAndSkip() {
        new JFXPanel();
    }

    private TransactionEditorPane createEditorPane() {
        var pane = new TransactionEditorPane(cache);
        pane.initControls();
        callPrivateMethod(pane, "onChangedTransactionTypes");
        callPrivateMethod(pane, "setupContactMenu");
        callPrivateMethod(pane, "setupAccountMenus");
        callPrivateMethod(pane, "setupComments");
        return pane;
    }

    private void setUserInput(TransactionEditorPane pane, Transaction t, String contactName) {
        // Transaction type
        pane.getTypeEdit().setText(t.getTransactionType().getTypeName());

        // Debited account
        pane.getDebitedAccountEdit().setText(cache.getAccount(t.getAccountDebitedUuid())
            .map(Account::getName)
            .orElse(""));

        // Credited account
        pane.getCreditedAccountEdit().setText(cache.getAccount(t.getAccountCreditedUuid())
            .map(Account::getName)
            .orElse(""));

        pane.getCommentEdit().setText(t.getComment());
        pane.getSumEdit().setText(t.getAmount().toString());
        pane.getCheckedCheckBox().setSelected(t.getChecked());

        t.getContactUuid().ifPresent(uuid -> pane.getContactEdit().setText(cache
            .getContact(uuid)
            .map(Contact::getName)
            .orElse("")));
        if (contactName != null) {
            pane.getContactEdit().setText(contactName);
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

    private void pressAddButton(TransactionEditorPane pane) {
        pane.getAddButton().fireEvent(new ActionEvent());
    }

    private void pressUpdateButton(TransactionEditorPane pane) {
        pane.getUpdateButton().fireEvent(new ActionEvent());
    }

    private void pressDeleteButton(TransactionEditorPane pane) {
        pane.getDeleteButton().fireEvent(new ActionEvent());
    }

    private Transaction createTestTransaction(Account debit, Account credit, Contact contact) {
        var now = LocalDate.now();
        var builder = new Transaction.Builder()
            .guid(UUID.randomUUID())
            .transactionType(TransactionType.CARD_PAYMENT)
            .accountCreditedType(category.getType())
            .accountDebitedType(category.getType())
            .accountCreditedCategoryUuid(category.getUuid())
            .accountDebitedCategoryUuid(category.getUuid())
            .accountDebitedUuid(debit.getUuid())
            .accountCreditedUuid(credit.getUuid())
            .day(now.getDayOfMonth())
            .month(now.getMonthValue())
            .year(now.getYear())
            .comment(UUID.randomUUID().toString())
            .amount(BigDecimal.valueOf(RANDOM.nextDouble()).setScale(2, RoundingMode.HALF_UP))
            .checked(RANDOM.nextBoolean());

        if (Objects.equals(debit.getCurrencyUuid(), credit.getCurrencyUuid())) {
            builder.rate(BigDecimal.ONE);
        } else {
            builder.rate(BigDecimal.valueOf(RANDOM.nextDouble()));
        }

        if (contact != null) {
            builder.contactUuid(contact.getUuid());
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
        Assert.assertEquals(r.getTransactionType(), t.getTransactionType(),
            "Transaction type ID is invalid");

        // Debited account
        Assert.assertEquals(r.getAccountDebitedUuid(), t.getAccountDebitedUuid(),
            "Debited account UUID is invalid");
        Assert.assertEquals(r.getAccountDebitedCategoryUuid(), t.getAccountDebitedCategoryUuid(),
            "Debited account category UUID is invalid");
        Assert.assertEquals(r.getAccountDebitedType(), t.getAccountDebitedType(),
            "Debited account category type ID is invalid");

        // Credited account
        Assert.assertEquals(r.getAccountCreditedUuid(), t.getAccountCreditedUuid(),
            "Credited account UUID is invalid");
        Assert.assertEquals(r.getAccountCreditedCategoryUuid(), t.getAccountCreditedCategoryUuid(),
            "Credited account category UUID is invalid");
        Assert.assertEquals(r.getAccountCreditedType(), t.getAccountCreditedType(),
            "Credited account category type ID is invalid");

        Assert.assertEquals(r.getDay(), t.getDay(), "Day is invalid");
        Assert.assertEquals(r.getComment(), t.getComment(), "Comment is invalid");
        Assert.assertEquals(r.getChecked(), t.getChecked(), "Checked status is invalid");
    }

    private void assertStatementRecord(TransactionEditorPane pane, StatementRecord record) {
        Assert.assertEquals(pane.getSumEdit().getText(), record.getAmount());
        Assert.assertEquals((int) pane.getDaySpinner().getValueFactory().getValue(),
            record.getActual().getDayOfMonth());
    }

    private void failOnEmptyBuilder() {
        Assert.fail("Builder is null");
    }

    @Test
    public void testNewTransactionSameCurrencyNoContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyExistingContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, null);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testNewTransactionSameCurrencyNewContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        var newContact = UUID.randomUUID().toString();

        Platform.runLater(() -> {
            var pane = createEditorPane();
            setUserInput(pane, transaction, newContact);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertEquals(c, newContact);
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var now = LocalDate.now();
            var resultedTransaction = builder
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
            assertMainFields(resultedTransaction, transaction);
            Assert.assertTrue(resultedTransaction.getContactUuid().isEmpty(), "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateSameCurrency() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertNotEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(), "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");

        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testEditorFieldsInitialStateDifferentCurrency() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_2, contact);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnAddTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertFalse(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressAddButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertNotEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), transaction.getRate(),
                "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(),
                "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testUpdatedTransactionSameCurrencyNoContact() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<Optional<Transaction.Builder>>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransaction(transaction);

            pane.setOnUpdateTransaction((builder, c) -> {
                Assert.assertTrue(c.isEmpty());
                Assert.assertTrue(pane.getRate1Edit().isDisabled());
                queue.add(Optional.ofNullable(builder));
            });

            pressUpdateButton(pane);
        });

        var result = queue.take();
        result.ifPresentOrElse(builder -> {
            var resultedTransaction = builder.build();
            Assert.assertEquals(resultedTransaction.getUuid(), transaction.getUuid());

            assertMainFields(resultedTransaction, transaction);
            Assert.assertEquals(resultedTransaction.getContactUuid(), transaction.getContactUuid(),
                "Contact UUID is invalid");
            Assert.assertEquals(resultedTransaction.getRate(), BigDecimal.ONE, "Rate is invalid");
            Assert.assertEquals(resultedTransaction.getAmount(), transaction.getAmount(), "Amount is invalid");
        }, this::failOnEmptyBuilder);
    }

    @Test
    public void testDeleteButton() throws Exception {
        var transaction = createTestTransaction(acc_1, acc_3, null);

        var queue = new ArrayBlockingQueue<UUID>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();

            callPrivateMethod(pane, "setupContactMenu");
            pane.setTransaction(transaction);
            pane.setOnDeleteTransaction(queue::add);

            pressDeleteButton(pane);
        });

        var resultedUuid = queue.take();
        Assert.assertEquals(resultedUuid, transaction.getUuid());
    }

    @Test
    public void testStatementRecord() throws Exception {
        var record = createTestStatementRecord();

        var queue = new ArrayBlockingQueue<TransactionEditorPane>(1);

        Platform.runLater(() -> {
            var pane = createEditorPane();
            pane.setTransactionFromStatement(record, null);
            queue.add(pane);
        });

        var pane = queue.take();
        assertStatementRecord(pane, record);
    }
}
