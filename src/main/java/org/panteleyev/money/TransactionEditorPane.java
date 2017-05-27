/*
 * Copyright (c) 2016, 2017, Petr Panteleyev <petr@panteleyev.org>
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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Contact;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.Named;
import org.panteleyev.money.persistence.Transaction;
import org.panteleyev.money.persistence.TransactionType;
import org.panteleyev.persistence.annotations.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TransactionEditorPane extends TitledPane implements Styles {

    private static abstract class BaseCompletionProvider<T> implements Callback<AutoCompletionBinding.ISuggestionRequest,Collection<T>>{
        private final Set<T> set;

        BaseCompletionProvider(Set<T> set) {
            this.set = set;
        }

        abstract String getElementString(T element);

        @Override
        public Collection<T> call(AutoCompletionBinding.ISuggestionRequest req) {
            if (req.getUserText().length() >= Options.getAutoCompleteLength()) {
                final String userText = req.getUserText();

                List<T> result = set.stream()
                        .filter(x -> getElementString(x).toLowerCase().contains(userText.toLowerCase()))
                        .collect(Collectors.toList());

                if (result.size() == 1 && getElementString(result.get(0)).equals(userText)) {
                    /* If there is a single case sensitive match then no suggestions must be shown. */
                    return Collections.emptyList();
                } else {
                    return result;
                }
            } else {
                return Collections.emptyList();
            }
        }
    }

    private static class CompletionProvider<T extends Named> extends BaseCompletionProvider<T> {
        CompletionProvider(Set<T> set) {
            super(set);
        }

        @Override
        String getElementString(T element) {
            return element.getName();
        }
    }

    private static class StringCompletionProvider extends BaseCompletionProvider<String> {
        StringCompletionProvider(Set<String> set) {
            super(set);
        }

        @Override
        String getElementString(String element) {
            return element;
        }
    }

    private final ResourceBundle    rb = ResourceBundle.getBundle("org.panteleyev.money.TransactionEditorPane");

    private final Spinner<Integer>  daySpinner = new Spinner<>();

    private final TextField         typeEdit = new TextField();
    private final TextField         debitedAccountEdit = new TextField();
    private final TextField         creditedAccountEdit = new TextField();
    private final TextField         contactEdit = new TextField();
    private final TextField         sumEdit = new TextField();
    private final CheckBox          checkedCheckBox = new CheckBox();
    private final TextField         commentEdit = new TextField();
    private final TextField         rate1Edit = new TextField();
    private final ComboBox<String>  rateDir1Combo = new ComboBox<>();
    private final TextField         invoiceNumberEdit = new TextField();
    private final Label             rateAmoutLabel = new Label();
    private final Label             debitedCategoryLabel = new Label();
    private final Label             creditedCategoryLabel = new Label();

    private final MenuButton        typeMenuButton = new MenuButton();
    private final MenuButton        debitedMenuButton = new MenuButton();
    private final MenuButton        creditedMenuButton = new MenuButton();
    private final MenuButton        contactMenuButton = new MenuButton();

    private final Button            addButton = new Button(rb.getString("addButton"));
    private final Button            updateButton = new Button(rb.getString("updateButton"));
    private final Button            deleteButton = new Button(rb.getString("deleteButton"));
    private final Button            clearButton = new Button(rb.getString("clearButton"));

    private BiConsumer<Transaction.Builder, String> addTransactionConsumer    = (t, c) -> {};
    private BiConsumer<Transaction.Builder, String> updateTransactionConsumer = (t, c) -> {};
    private Consumer<Integer>                       deleteTransactionConsumer = t -> {};

    private Transaction.Builder     builder = new Transaction.Builder();

    private final Set<TransactionType>  typeSuggestions = new TreeSet<>();
    private final Set<Contact>          contactSuggestions = new TreeSet<>();
    private final Set<Account>          debitedSuggestions = new TreeSet<>();
    private final Set<Account>          creditedSuggestions = new TreeSet<>();
    private final Set<String>           commentSuggestions = new TreeSet<>();

    private final ValidationSupport     validation = new ValidationSupport();

    private final BooleanProperty       newTransactionProperty = new SimpleBooleanProperty(true);

    private final Validator<String> DECIMAL_VALIDATOR = (Control control, String value) -> {
        boolean invalid = false;
        try {
            new BigDecimal(value);
            updateRateAmount();
        } catch (NumberFormatException ex) {
            invalid = true;
        }
        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    private String newContactName;

    private final MapChangeListener<Integer, Contact> contactListener =
            l -> Platform.runLater(this::setupContactMenu);
    private final MapChangeListener<Integer, Account> accountListener =
            l -> Platform.runLater(this::setupAccountMenus);
    private final MapChangeListener<Integer, Transaction> transactionListener = this::transactionChangeListener;

    private static final StringConverter<TransactionType> TRANSACTION_TYPE_TO_STRING = new ToStringConverter<TransactionType>() {
        @Override
        public String toString(TransactionType object) {
            return object.getName();
        }
    };

    private static final StringConverter<Contact> CONTACT_TO_STRING = new ToStringConverter<Contact>() {
        @Override
        public String toString(Contact object) {
            return object.getName();
        }
    };

    private static final StringConverter<Account> ACCOUNT_TO_STRING = new ToStringConverter<Account>() {
        @Override
        public String toString(Account object) {
            return object.getName();
        }
    };

    public TransactionEditorPane() {
        initialize();
    }

    private void initialize() {
        VBox debitedBox = new VBox(SMALL_SPACING, new Label(rb.getString("debitedAccountLabel")),
                new HBox(debitedAccountEdit, debitedMenuButton),
                debitedCategoryLabel);
        HBox.setHgrow(debitedAccountEdit, Priority.ALWAYS);

        VBox creditedBox = new VBox(SMALL_SPACING, new Label(rb.getString("creditedAccountLabel")),
                new HBox(creditedAccountEdit, creditedMenuButton),
                creditedCategoryLabel);
        HBox.setHgrow(creditedAccountEdit, Priority.ALWAYS);

        VBox contactBox = new VBox(SMALL_SPACING, new Label(rb.getString("contactLabel")),
                new HBox(contactEdit, contactMenuButton));
        HBox.setHgrow(contactEdit, Priority.ALWAYS);

        HBox hBox1 = new HBox(5, sumEdit, checkedCheckBox);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        VBox sumBox = new VBox(SMALL_SPACING, new Label(rb.getString("sumLabel")), hBox1);

        VBox commentBox = new VBox(SMALL_SPACING, new Label(rb.getString("commentLabel")), commentEdit);

        VBox rateBox = new VBox(SMALL_SPACING, new Label(rb.getString("rateLabel")),
                new HBox(rate1Edit, rateDir1Combo),
                rateAmoutLabel);

        Region filler = new Region();

        clearButton.setOnAction((ae) -> onClearButton());
        clearButton.setCancelButton(true);

        addButton.setOnAction((ae) -> onAddButton());
        addButton.setDefaultButton(true);

        updateButton.setOnAction((ae) -> onUpdateButton());

        deleteButton.setOnAction((ae) -> onDeleteButton());

        HBox row3 = new HBox(BIG_SPACING,
                new VBox(2, new Label(rb.getString("invoiceLabel")),  invoiceNumberEdit),
                filler, clearButton, deleteButton, updateButton, addButton);
        row3.setAlignment(Pos.CENTER_LEFT);

        setContent(new VBox(BIG_SPACING,
                new HBox(BIG_SPACING,
                        new VBox(2, new Label(rb.getString("dayLabel")), daySpinner),
                        new VBox(2, new Label(rb.getString("typeLabel")),
                                new HBox(typeEdit, typeMenuButton)),
                        debitedBox, creditedBox, contactBox, sumBox),
                new HBox(BIG_SPACING, commentBox, rateBox),
                row3)
        );

        HBox.setHgrow(debitedBox, Priority.ALWAYS);
        HBox.setHgrow(creditedBox, Priority.ALWAYS);
        HBox.setHgrow(contactBox, Priority.ALWAYS);
        HBox.setHgrow(commentBox, Priority.ALWAYS);
        HBox.setHgrow(filler, Priority.ALWAYS);

        typeMenuButton.setFocusTraversable(false);
        checkedCheckBox.setFocusTraversable(false);
        debitedMenuButton.setFocusTraversable(false);
        creditedMenuButton.setFocusTraversable(false);
        contactMenuButton.setFocusTraversable(false);

        rate1Edit.setDisable(true);
        rate1Edit.setPrefColumnCount(5);

        rateDir1Combo.setDisable(true);

        rateAmoutLabel.getStyleClass().add(RATE_LABEL);
        debitedCategoryLabel.getStyleClass().add(SUB_LABEL);
        creditedCategoryLabel.getStyleClass().add(SUB_LABEL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        setText(rb.getString("title") + " ###");

        daySpinner.setEditable(true);
        daySpinner.getEditor().prefColumnCountProperty().set(4);
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1, 1);
        valueFactory.setValue(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        daySpinner.setValueFactory(valueFactory);

        deleteButton.disableProperty().bind(newTransactionProperty);
        updateButton.disableProperty().bind(validation.invalidProperty().or(newTransactionProperty));
        addButton.disableProperty().bind(validation.invalidProperty());

        rateDir1Combo.getItems().setAll("/", "*");

        TextFields.bindAutoCompletion(typeEdit, new CompletionProvider<>(typeSuggestions), TRANSACTION_TYPE_TO_STRING);
        TextFields.bindAutoCompletion(debitedAccountEdit, new CompletionProvider<>(debitedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(creditedAccountEdit, new CompletionProvider<>(creditedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(contactEdit, new CompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);
        TextFields.bindAutoCompletion(commentEdit, new StringCompletionProvider(commentSuggestions));

        Platform.runLater(this::createValidationSupport);

        creditedAccountEdit.focusedProperty().addListener((x,oldValue,newValue) -> {
            if (oldValue && !newValue) {
                processAutoFill();
            }
        });

        typeEdit.focusedProperty().addListener((x,oldValue,newValue) -> {
            if (oldValue && !newValue) {
                autoFillType();
            }
        });

        final MoneyDAO dao = MoneyDAO.getInstance();
        dao.contacts().addListener(contactListener);
        dao.accounts().addListener(accountListener);
        dao.transactions().addListener(transactionListener);

        dao.preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(() -> {
                    onChangedTransactionTypes();
                    setupAccountMenus();
                    setupContactMenu();
                    setupComments();
                });
            }
        });
    }

    public final void initControls() {
        contactEdit.setText("");

        typeMenuButton.getItems().clear();
        debitedMenuButton.getItems().clear();
        creditedMenuButton.getItems().clear();
        contactMenuButton.getItems().clear();

        typeSuggestions.clear();
        contactSuggestions.clear();
        debitedSuggestions.clear();
        creditedSuggestions.clear();
        commentSuggestions.clear();
    }

    private void setupBanksAndCashMenuItems(Set<Account> debitedSuggestions, Set<Account> creditedSuggestions) {
        List<Account> banksAnsCash = MoneyDAO.getInstance().getAccountsByType(CategoryType.BANKS_AND_CASH);
        banksAnsCash.stream()
            .sorted((Account o1, Account o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
            .filter(Account::isEnabled)
            .forEach(bac -> {
                String title = "[" + bac.getName() + "]";
                MenuItem m1 = new MenuItem(title);
                m1.setOnAction(e1 -> onDebitedAccountSelected(bac));
                MenuItem m2 = new MenuItem(title);
                m2.setOnAction(e1 -> onCreditedAccountSelected(bac));

                debitedMenuButton.getItems().add(m1);
                creditedMenuButton.getItems().add(m2);

                debitedSuggestions.add(bac);
                creditedSuggestions.add(bac);
            });
        if (!banksAnsCash.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    private void setupDebtMenuItems(Set<Account> debitedSuggestions, Set<Account> creditedSuggestions) {
        setAccountMenuItemsByCategory(CategoryType.DEBTS, "!", debitedSuggestions, creditedSuggestions);
    }

    private void setupAssetsMenuItems(Set<Account> debitedSuggestions, Set<Account> creditedSuggestions) {
        setAccountMenuItemsByCategory(CategoryType.ASSETS, ".", debitedSuggestions, creditedSuggestions);
    }

    private void setAccountMenuItemsByCategory(CategoryType categoryType, String prefix, Set<Account> debitedSuggestions, Set<Account> creditedSuggestions) {
        List<Category> categories = MoneyDAO.getInstance().getCategoriesByType(categoryType);
        categories.stream()
            .sorted((Category o1, Category o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
            .forEach(x -> {
                List<Account> accounts = MoneyDAO.getInstance().getAccountsByCategory(x.getId());

                if (!accounts.isEmpty()) {
                    debitedMenuButton.getItems().add(new MenuItem(x.getName()));
                    creditedMenuButton.getItems().add(new MenuItem(x.getName()));

                    accounts.stream()
                        .filter(Account::isEnabled)
                        .forEach(acc -> {
                            String title = "  " + prefix + " " + acc.getName();
                            MenuItem m1 = new MenuItem(title);
                            m1.setOnAction(e1 -> onDebitedAccountSelected(acc));
                            MenuItem m2 = new MenuItem(title);
                            m2.setOnAction(e1 -> onCreditedAccountSelected(acc));

                            debitedMenuButton.getItems().add(m1);
                            creditedMenuButton.getItems().add(m2);

                            debitedSuggestions.add(acc);
                            creditedSuggestions.add(acc);
                        });
                }
            });
        if (!categories.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    public void clear() {
        builder = new Transaction.Builder();

        newTransactionProperty.set(true);

        daySpinner.getValueFactory().setValue(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        typeEdit.setText("");
        creditedAccountEdit.setText("");
        debitedAccountEdit.setText("");
        contactEdit.setText("");
        checkedCheckBox.setSelected(false);
        commentEdit.setText("");
        checkedCheckBox.setSelected(false);
        invoiceNumberEdit.setText("");
        sumEdit.setText("");
        rateAmoutLabel.setText("");


        daySpinner.getEditor().setText(Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
        daySpinner.getEditor().selectAll();
        daySpinner.requestFocus();
    }

    public void setTransaction(Transaction tr) {
        builder = new Transaction.Builder(tr);

        newTransactionProperty.set(false);

        MoneyDAO dao = MoneyDAO.getInstance();

        setText(rb.getString("title") + " #" + tr.getId());

        // Type
        typeEdit.setText(tr.getTransactionType().getName());

        // Accounts
        Optional<Account> accCredited = dao.getAccount(tr.getAccountCreditedId());
        creditedAccountEdit.setText(accCredited.map(Account::getName).orElse(""));

        Optional<Account> accDebited = dao.getAccount(tr.getAccountDebitedId());
        debitedAccountEdit.setText(accDebited.map(Account::getName).orElse(""));

        contactEdit.setText(dao.getContact(tr.getContactId())
                .map(Contact::getName).orElse(""));

        // Other fields
        commentEdit.setText(tr.getComment());
        checkedCheckBox.setSelected(tr.isChecked());
        invoiceNumberEdit.setText(tr.getInvoiceNumber());

        // Rate

        int debitedCurrencyId = accDebited.map(Account::getCurrencyId).orElse(0);
        int creditedCurrencyId = accCredited.map(Account::getCurrencyId).orElse(0);

        if (debitedCurrencyId == creditedCurrencyId) {
            rate1Edit.setDisable(true);
            rate1Edit.setText("");
        } else {
            rate1Edit.setDisable(false);

            BigDecimal rate = tr.getRate();
            if (BigDecimal.ZERO.compareTo(rate) == 0) {
                rate = BigDecimal.ONE.setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP);
            }
            if (rate != null) {
                rate1Edit.setText(rate.toString());
                rateDir1Combo.getSelectionModel().select(tr.getRateDirection());
            } else {
                rate1Edit.setText("");
            }
        }

        // Day
        daySpinner.getValueFactory().setValue(tr.getDay());

        // Sum
        sumEdit.setText(tr.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        updateRateAmount();
    }

    private void onClearButton() {
        clear();
    }

    private void onDeleteButton() {
        if (builder.id() != 0) {
            deleteTransactionConsumer.accept(builder.id());
        }
    }

    private void onUpdateButton() {
        if (buildTransaction()) {
            updateTransactionConsumer.accept(builder, newContactName);
        }
    }

    private void onAddButton() {
        if (buildTransaction()) {
            addTransactionConsumer.accept(builder, newContactName);
        }
    }

    private void onContactSelected(Contact c) {
        contactEdit.setText(c.getName());
        builder.contactId(c.getId());
    }

    private void onDebitedAccountSelected(Account acc) {
        debitedAccountEdit.setText(acc.getName());
        enableDisableRate();
    }

    private void onCreditedAccountSelected(Account acc) {
        creditedAccountEdit.setText(acc.getName());
        enableDisableRate();
    }

    private void onTransactionTypeSelected(TransactionType type) {
        typeEdit.setText(type.getName());
    }

    public void setOnAddTransaction(BiConsumer<Transaction.Builder, String> c) {
        addTransactionConsumer = c;
    }

    public void setOnUpdateTransaction(BiConsumer<Transaction.Builder, String> c) {
        updateTransactionConsumer = c;
    }

    public void setOnDeleteTransaction(Consumer<Integer> c) {
        deleteTransactionConsumer = c;
    }

    private boolean buildTransaction() {
        // Check type id
        handleTypeFocusLoss();
        Optional<TransactionType> type = checkTextFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING);
        type.ifPresent(t -> builder.transactionType(t));

        Optional<Account> debitedAccount = checkTextFieldValue(debitedAccountEdit, debitedSuggestions, ACCOUNT_TO_STRING);
        if (debitedAccount.isPresent()) {
            Account acc = debitedAccount.get();
            builder.accountDebitedId(acc.getId());
            builder.accountDebitedCategoryId(acc.getCategoryId());
            builder.accountDebitedType(acc.getType());
        } else {
            return false;
        }

        Optional<Account> creditedAccount = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
        if (creditedAccount.isPresent()) {
            Account acc = creditedAccount.get();
            builder.accountCreditedId(acc.getId());
            builder.accountCreditedCategoryId(acc.getCategoryId());
            builder.accountCreditedType(acc.getType());
        } else {
            return false;
        }

//        builder.day(daySpinner.getValue());
        builder.comment(commentEdit.getText());
        builder.checked(checkedCheckBox.isSelected());
        builder.invoiceNumber(invoiceNumberEdit.getText());

        try {
            builder.day(Integer.parseInt(daySpinner.getEditor().getText()));

            builder.amount(new BigDecimal(sumEdit.getText()));

            if (!rate1Edit.isDisabled()) {
                builder.rate(new BigDecimal(rate1Edit.getText()));
                builder.rateDirection(rateDir1Combo.getSelectionModel().getSelectedIndex());
            } else {
                builder.rate(BigDecimal.ONE);
                builder.rateDirection(1);
            }
        } catch (NumberFormatException ex) {
            return false;
        }

        newContactName = null;

        String contactName = contactEdit.getText();
        if (contactName == null || contactName.isEmpty()) {
            builder.contactId(0);
        } else {
            Optional<Contact> contact = checkTextFieldValue(contactName, contactSuggestions, CONTACT_TO_STRING);
            if (contact.isPresent()) {
                builder.contactId(contact.get().getId());
            } else {
                newContactName = contactName;
            }
        }

        return true;
    }

    private void enableDisableRate() {
        boolean disable;

        if (builder.getAccountCreditedId() == 0 || builder.getAccountDebitedId() == 0) {
            disable = true;
        } else {
            MoneyDAO dao = MoneyDAO.getInstance();

            int c1 = dao.getAccount(builder.getAccountDebitedId())
                    .map(Account::getCurrencyId)
                    .orElse(0);

            int c2 = dao.getAccount(builder.getAccountCreditedId())
                    .map(Account::getCurrencyId)
                    .orElse(0);

            disable = c1 == c2;
        }

        rate1Edit.setDisable(disable);
        rateDir1Combo.setDisable(disable);

        if (!disable && rate1Edit.getText().isEmpty()) {
            rate1Edit.setText("1");
            rateDir1Combo.getSelectionModel().select(0);
        }
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value, Collection<T> items, StringConverter<T> converter) {
        return items.stream()
            .filter(item -> converter.toString(item).equals(value))
            .findFirst();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(TextField field, Collection<T> items, StringConverter<T> converter) {
        return checkTextFieldValue(field.getText(), items, converter);
    }

    private void handleTypeFocusLoss() {
        Optional<TransactionType> type = checkTextFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING);
        if (!type.isPresent()) {
            typeEdit.setText(TransactionType.UNDEFINED.getName());
        }
    }

    private void createValidationSupport() {
        validation.registerValidator(typeEdit, (Control control, String value) -> {
            Optional<TransactionType> type = checkTextFieldValue(typeEdit, typeSuggestions, TRANSACTION_TYPE_TO_STRING);
            return ValidationResult.fromErrorIf(control, null, !type.isPresent());
        });

        validation.registerValidator(debitedAccountEdit, (Control control, String value) -> {
            Optional<Account> account = checkTextFieldValue(debitedAccountEdit, debitedSuggestions, ACCOUNT_TO_STRING);
            updateCategoryLabel(debitedCategoryLabel, account.orElse(null));

            builder.accountDebitedId(account.map(Account::getId).orElse(0));

            enableDisableRate();
            return ValidationResult.fromErrorIf(control, null, !account.isPresent());
        });

        validation.registerValidator(creditedAccountEdit, (Control control, String value) -> {
            Optional<Account> account = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
            updateCategoryLabel(creditedCategoryLabel, account.orElse(null));

            builder.accountCreditedId(account.map(Account::getId).orElse(0));

            enableDisableRate();
            return ValidationResult.fromErrorIf(control, null, !account.isPresent());
        });

        validation.registerValidator(sumEdit, DECIMAL_VALIDATOR);
        validation.registerValidator(rate1Edit, false, DECIMAL_VALIDATOR);

        validation.initInitialDecoration();
    }

    private void setupContactMenu() {
        contactMenuButton.getItems().clear();
        contactSuggestions.clear();

        Collection<Contact> contacts = MoneyDAO.getInstance().getContacts();

        contacts.stream()
            .sorted((Contact o1, Contact o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
            .forEach(x -> {
                MenuItem m = new MenuItem(x.getName());
                m.setOnAction(event -> onContactSelected(x));
                contactMenuButton.getItems().add(m);
                contactSuggestions.add(x);
            });

        contactMenuButton.setDisable(contactMenuButton.getItems().isEmpty());
    }

    private void onChangedTransactionTypes() {
        typeMenuButton.getItems().clear();
        typeSuggestions.clear();

        Arrays.stream(TransactionType.values()).forEach(x -> {
            if (x.isSeparator()) {
                typeMenuButton.getItems().add(new SeparatorMenuItem());
            } else {
                MenuItem m = new MenuItem(x.getName());
                m.setOnAction(event -> onTransactionTypeSelected(x));
                typeMenuButton.getItems().add(m);
                typeSuggestions.add(x);
            }
        });
    }

    private void setupAccountMenus() {
        debitedMenuButton.getItems().clear();
        creditedMenuButton.getItems().clear();
        debitedSuggestions.clear();
        creditedSuggestions.clear();

        // Bank and cash accounts first
        setupBanksAndCashMenuItems(debitedSuggestions, creditedSuggestions);

        // Incomes to debitable accounts
        List<Category> incomeCategories = MoneyDAO.getInstance().getCategoriesByType(CategoryType.INCOMES);
        incomeCategories.stream()
            .sorted((Category o1, Category o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
            .forEach(x -> {
                List<Account> accounts = MoneyDAO.getInstance().getAccountsByCategory(x.getId());

                if (!accounts.isEmpty()) {
                    debitedMenuButton.getItems().add(new MenuItem(x.getName()));

                    accounts.forEach(acc -> {
                        MenuItem accMenuItem = new MenuItem("  + " + acc.getName());
                        accMenuItem.setOnAction(e1 -> onDebitedAccountSelected(acc));
                        debitedMenuButton.getItems().add(accMenuItem);
                        debitedSuggestions.add(acc);
                    });
                }
            });
        if (!incomeCategories.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
        }

        // Expenses to creditable accounts
        List<Category> expenseCategories = MoneyDAO.getInstance().getCategoriesByType(CategoryType.EXPENSES);
        expenseCategories.stream()
            .sorted((Category o1, Category o2) -> o1.getName().compareToIgnoreCase(o2.getName()))
            .forEach(x -> {
                List<Account> accounts = MoneyDAO.getInstance().getAccountsByCategory(x.getId());

                if (!accounts.isEmpty()) {
                    creditedMenuButton.getItems().add(new MenuItem(x.getName()));

                    accounts.forEach(acc -> {
                        creditedSuggestions.add(acc);
                        MenuItem accMenuItem = new MenuItem("  - " + acc.getName());
                        accMenuItem.setOnAction(e1 -> onCreditedAccountSelected(acc));
                        creditedMenuButton.getItems().add(accMenuItem);
                    });
                }
            });
        if (!expenseCategories.isEmpty()) {
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }

        setupDebtMenuItems(debitedSuggestions, creditedSuggestions);
        setupAssetsMenuItems(debitedSuggestions, creditedSuggestions);
    }

    private void setupComments() {
        commentSuggestions.clear();
        commentSuggestions.addAll(MoneyDAO.getInstance().getUniqueTransactionComments());
    }

    private void transactionChangeListener(MapChangeListener.Change<? extends Integer, ? extends Transaction> change) {
        if (change.wasAdded()) {
            String comment = change.getValueAdded().getComment();
            if (comment != null && !comment.isEmpty()) {
                commentSuggestions.add(comment);
            }
        }
    }

    private void updateRateAmount() {
        String amount = sumEdit.getText();
        if (amount.isEmpty()) {
            amount = "0";
        }

        BigDecimal amountValue = new BigDecimal(amount)
                .setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP);

        String rate = rate1Edit.getText();
        if (rate.isEmpty()) {
            rate = "1";
        }

        BigDecimal rateValue = new BigDecimal(rate)
                .setScale(Field.SCALE, BigDecimal.ROUND_HALF_UP);

        BigDecimal total;

        if (rateDir1Combo.getSelectionModel().getSelectedIndex() == 0) {
            total = amountValue.divide(rateValue, BigDecimal.ROUND_HALF_UP);
        } else {
            total = amountValue.multiply(rateValue);
        }

        Platform.runLater(() -> rateAmoutLabel
                .setText("= " + total.setScale(2, BigDecimal.ROUND_HALF_UP).toString()));
    }

    private void updateCategoryLabel(Label label, Account account) {
        if (account != null) {
            MoneyDAO dao = MoneyDAO.getInstance();
            String catName = dao.getCategory(account.getCategoryId())
                    .map(Category::getName)
                    .orElse("");
            label.setText(account.getType().getName() + " | " + catName);
        } else {
            label.setText("");
        }
    }

    private void processAutoFill() {
        final int accDebitedId = builder.getAccountDebitedId();
        final int accCreditedId = builder.getAccountCreditedId();

        if (accDebitedId != 0 && accCreditedId != 0) {
            MoneyDAO.getInstance().getTransactions().stream()
                    .filter(t -> t.getAccountCreditedId() == accCreditedId && t.getAccountDebitedId() == accDebitedId)
                    .max(Transaction.BY_DATE)
                    .ifPresent(t -> {
                        if (commentEdit.getText().isEmpty()) {
                            commentEdit.setText(t.getComment());
                        }
                        if (sumEdit.getText().isEmpty()) {
                            sumEdit.setText(t.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                        }
                        MoneyDAO.getInstance().getContact(t.getContactId())
                                .ifPresent(c -> {
                                    if (contactEdit.getText().isEmpty()) {
                                        contactEdit.setText(c.getName());
                                    }
                                });
                    });
        }
    }

    private void autoFillType() {
        if (typeEdit.getText().isEmpty()) {
            typeEdit.setText(TransactionType.UNDEFINED.getName());
        }
    }
}
