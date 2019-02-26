/*
 * Copyright (c) 2017, 2019, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.details.TransactionDetail;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Contact;
import org.panteleyev.money.persistence.model.Transaction;
import org.panteleyev.money.statements.StatementRecord;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Logger;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;
import static org.panteleyev.money.persistence.dto.Dto.dtoClass;

final class TransactionsTab extends BorderPane implements TransactionTableView.TransactionDetailsCallback {
    private final static Logger LOGGER = Logger.getLogger(TransactionsTab.class.getName());

    private final ChoiceBox<Object> accountFilterBox = new ChoiceBox<>();
    private final ChoiceBox<String> monthFilterBox = new ChoiceBox<>();
    private final Spinner<Integer> yearSpinner = new Spinner<>();
    private final Label transactionCountLabel = new Label();

    private final TransactionTableView transactionTable =
        new TransactionTableView(TransactionTableView.Mode.ACCOUNT, this);
    private final TransactionEditorPane transactionEditor = new TransactionEditorPane();

    private final MapChangeListener<Integer, Account> accountListener = change -> {
        Platform.runLater(this::initAccountFilterBox);
        Platform.runLater(this::reloadTransactions);
    };

    TransactionsTab() {
        var prevButton = new Button("", getButtonImage("arrow-left-16.png"));
        prevButton.setOnAction(event -> onPrevMonth());

        var todayButton = new Button("", getButtonImage("bullet-black-16.png"));
        todayButton.setOnAction(event -> onCurrentMonth());

        var nextButton = new Button("", getButtonImage("arrow-right-16.png"));
        nextButton.setOnAction(event -> onNextMonth());

        monthFilterBox.setOnAction(event -> onMonthChanged());

        var hBox = new HBox(5.0,
            accountFilterBox,
            monthFilterBox,
            yearSpinner,
            prevButton,
            todayButton,
            nextButton,
            new Label("Transactions:"),
            transactionCountLabel
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        setTop(hBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        transactionTable.setOnMouseClicked(event -> onTransactionSelected());

        transactionCountLabel.textProperty().bind(transactionTable.listSizeProperty().asString());

        setCenter(transactionTable);
        setBottom(transactionEditor);

        for (int i = 1; i <= 12; i++) {
            monthFilterBox.getItems()
                .add(Month.of(i).getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
        }

        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory
            .IntegerSpinnerValueFactory(1970, 2050);
        yearSpinner.setValueFactory(valueFactory);
        yearSpinner.valueProperty().addListener((x, y, z) -> Platform.runLater(this::reloadTransactions));

        transactionEditor.setOnAddTransaction(this::onAddTransaction);
        transactionEditor.setOnUpdateTransaction(this::onUpdateTransaction);
        transactionEditor.setOnDeleteTransaction(this::onDeleteTransaction);

        transactionTable.setOnCheckTransaction(this::onCheckTransaction);

        setCurrentDate();

        accountFilterBox.setConverter(new ReadOnlyStringConverter<>() {
            public String toString(Object obj) {
                if (obj instanceof String) {
                    return obj.toString();
                }

                if (obj instanceof Account) {
                    Account account = (Account) obj;
                    switch (account.getType()) {
                        case BANKS_AND_CASH:
                            return "[" + account.getName() + "]";
                        case INCOMES:
                            return "+ " + account.getName();
                        case EXPENSES:
                            return "- " + account.getName();
                        case DEBTS:
                            return "! " + account.getName();
                        case ASSETS:
                            return ". " + account.getName();
                        default:
                            return account.getName();
                    }
                }

                return null;
            }
        });

        accountFilterBox.getSelectionModel().selectedIndexProperty()
            .addListener((x, y, z) -> Platform.runLater(this::reloadTransactions));

        getDao().accounts().addListener(accountListener);

        getDao().preloadingProperty().addListener(
            (x, y, newValue) -> {
                if (!newValue) {
                    Platform.runLater(this::initAccountFilterBox);
                    Platform.runLater(this::reloadTransactions);
                }
            });
    }

    TransactionEditorPane getTransactionEditor() {
        return transactionEditor;
    }

    private ImageView getButtonImage(String name) {
        var image = new ImageView(new Image("/org/panteleyev/money/res/" + name));
        image.setFitHeight(16.0);
        image.setFitWidth(16.0);
        return image;
    }

    private void addAccountsToChoiceBox(Collection<Account> aList) {
        if (!aList.isEmpty()) {
            accountFilterBox.getItems().add(new Separator());

            aList.stream()
                .filter(Account::getEnabled)
                .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
                .forEach(account -> accountFilterBox.getItems().add(account));
        }
    }

    private void initAccountFilterBox() {
        accountFilterBox.getItems().setAll(RB.getString("text.All.Accounts"));

        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.BANKS_AND_CASH));
        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.DEBTS));
        addAccountsToChoiceBox(getDao().getAccountsByType(CategoryType.ASSETS));

        accountFilterBox.getSelectionModel().clearAndSelect(0);
    }

    private void setCurrentDate() {
        var now = LocalDate.now();
        monthFilterBox.getSelectionModel().select(now.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(now.getYear());
    }

    private void setDate(LocalDate date) {
        monthFilterBox.getSelectionModel().select(date.getMonth().getValue() - 1);
        yearSpinner.getValueFactory().setValue(date.getYear());
    }

    private void onPrevMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() - 1;

        if (month < 0) {
            month = 11;
            yearSpinner.getValueFactory().decrement(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onNextMonth() {
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;

        if (month == 12) {
            month = 0;
            yearSpinner.getValueFactory().increment(1);
        }

        monthFilterBox.getSelectionModel().select(month);

        reloadTransactions();
    }

    private void onCurrentMonth() {
        setCurrentDate();

        reloadTransactions();
    }

    private void reloadTransactions() {
        transactionTable.clear();
        transactionTable.getSelectionModel().select(null);

        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        Predicate<Transaction> filter = t -> t.getMonth() == month
            && t.getYear() == year;
//            && t.getParentId() == 0;

        var selected = accountFilterBox.getSelectionModel().getSelectedItem();
        if (selected instanceof Account) {
            int id = ((Account) selected).getId();
            filter = filter.and(t -> t.getAccountCreditedId() == id || t.getAccountDebitedId() == id);
        }

        transactionTable.setTransactionFilter(filter);
        transactionTable.sort();
    }

    private void onMonthChanged() {
        reloadTransactions();
    }

    private void onTransactionSelected() {
        transactionEditor.clear();
        if (transactionTable.getSelectedTransactionCount() == 1) {
            transactionTable.getSelectedTransaction().ifPresent(transactionEditor::setTransaction);
        }
    }

    private Contact createContact(String name) {
        var contact = new Contact.Builder()
            .id(getDao().generatePrimaryKey(dtoClass(Contact.class)))
            .name(name)
            .build();

        getDao().insertContact(contact);
        return contact;
    }

    private void onAddTransaction(Transaction.Builder builder, String c) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        int year = yearSpinner.getValue();

        builder.id(getDao().generatePrimaryKey(dtoClass(Transaction.class)))
            .month(month)
            .year(year);

        transactionEditor.clear();
        getDao().insertTransaction(builder.build());
    }

    private void onUpdateTransaction(Transaction.Builder builder, String c) {
        // contact
        if (c != null && !c.isEmpty()) {
            builder.contactId(createContact(c).getId());
        }

        // date
        int month = monthFilterBox.getSelectionModel().getSelectedIndex() + 1;
        builder.month(month);
        int year = yearSpinner.getValue();
        builder.year(year);

        transactionEditor.clear();
        getDao().updateTransaction(builder.build());
    }

    private void onDeleteTransaction(int id) {
        new Alert(Alert.AlertType.CONFIRMATION, "Are you sure to delete this transaction?")
            .showAndWait()
            .ifPresent(r -> {
                if (r == ButtonType.OK) {
                    transactionEditor.clear();
                    getDao().deleteTransaction(id);
                }
            });
    }

    private void onCheckTransaction(List<Transaction> transactions, boolean check) {
        for (var t : transactions) {
            getDao().updateTransaction(t.check(check));
        }
    }

    void scrollToEnd() {
        if (transactionTable.getDayColumn().getSortType().equals(TableColumn.SortType.ASCENDING)) {
            transactionTable.scrollTo(getDao().transactions().size() - 1);
        } else {
            transactionTable.scrollTo(0);
        }
    }

    public void handleStatementRecord(StatementRecord record, Account account) {
        Platform.runLater(() -> {
            setDate(record.getActual());
            transactionEditor.setTransactionFromStatement(record, account);
        });
    }

    @Override
    public void handleTransactionDetails(Transaction transaction, List<TransactionDetail> details) {
        var childTransactions = getDao().getTransactionDetails(transaction);

        if (details.isEmpty()) {
            if (!childTransactions.isEmpty()) {
                for (Transaction child : childTransactions) {
                    getDao().deleteTransaction(child.getId());
                }
                var noChildren = new Transaction.Builder(transaction)
                    .detailed(false)
                    .timestamp()
                    .build();
                getDao().updateTransaction(noChildren);
            }
        } else {
            getDao().updateTransaction(new Transaction.Builder(transaction)
                .detailed(true)
                .timestamp()
                .build());

            for (Transaction ch : childTransactions) {
                getDao().deleteTransaction(ch.getId());
            }

            for (var transactionDetail : details) {
                var newDetail = new Transaction.Builder(transaction)
                    .id(getDao().generatePrimaryKey(dtoClass(Transaction.class)))
                    .accountCreditedId(transactionDetail.getAccountCreditedId())
                    .amount(transactionDetail.getAmount())
                    .comment(transactionDetail.getComment())
                    .guid(UUID.randomUUID().toString())
                    .parentId(transaction.getId())
                    .detailed(false)
                    .timestamp()
                    .build();
                getDao().insertTransaction(newDetail);
            }
        }
    }
}
