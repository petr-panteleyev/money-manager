/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.statements.StatementRecord;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import static javafx.application.Platform.runLater;
import static javafx.scene.input.KeyCode.LEFT;
import static javafx.scene.input.KeyCode.RIGHT;
import static javafx.scene.input.KeyCode.UP;
import static javafx.scene.input.KeyCombination.ALT_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.KeyCombination.SHORTCUT_DOWN;
import static javafx.scene.layout.Priority.ALWAYS;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.DOUBLE_SPACING;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;
import static org.panteleyev.money.persistence.MoneyDAO.FIELD_SCALE;

public final class TransactionDialog extends BaseDialog<Transaction.Builder> {
    private static final ToStringConverter<TransactionType> TRANSACTION_TYPE_TO_STRING =
        new ToStringConverter<>() {
            public String toString(TransactionType obj) {
                return obj.toString();
            }
        };

    private static final ToStringConverter<Contact> CONTACT_TO_STRING = new ToStringConverter<>() {
        public String toString(Contact obj) {
            return obj.name();
        }
    };

    private static final ToStringConverter<Account> ACCOUNT_TO_STRING = new ToStringConverter<>() {
        public String toString(Account obj) {
            return obj.name();
        }
    };

    private static class CompletionProvider<T extends Named> extends BaseCompletionProvider<T> {
        CompletionProvider(Set<T> set) {
            super(set, Options::getAutoCompleteLength);
        }

        public String getElementString(T element) {
            return element.name();
        }
    }

    private static class TransactionTypeCompletionProvider extends BaseCompletionProvider<TransactionType> {
        TransactionTypeCompletionProvider(Set<TransactionType> set) {
            super(set, Options::getAutoCompleteLength);
        }


        public String getElementString(TransactionType element) {
            return element.toString();
        }
    }

    private static class StringCompletionProvider extends BaseCompletionProvider<String> {
        StringCompletionProvider(Set<String> set) {
            super(set, Options::getAutoCompleteLength);
        }

        public String getElementString(String element) {
            return element;
        }
    }

    private final DataCache cache;

    private UUID uuid;      // current transaction uuid if any

    private final DatePicker datePicker = new DatePicker();

    private static LocalDate lastSelectedDate = LocalDate.now();

    private final TextField typeEdit = new TextField();
    private final TextField debitedAccountEdit = new TextField();
    private final TextField creditedAccountEdit = new TextField();
    private final TextField contactEdit = new TextField();
    private final TextField sumEdit = new TextField();
    private final CheckBox checkedCheckBox = new CheckBox();
    private final TextField commentEdit = new TextField();
    private final TextField rate1Edit = new TextField();
    private final ComboBox<String> rateDir1Combo = new ComboBox<>();
    private final TextField invoiceNumberEdit = new TextField();
    private final Label rateAmoutLabel = new Label();
    private final Label debitedCategoryLabel = new Label();
    private final Label creditedCategoryLabel = new Label();

    private final MenuButton typeMenuButton = new MenuButton();
    private final MenuButton debitedMenuButton = new MenuButton();
    private final MenuButton creditedMenuButton = new MenuButton();
    private final MenuButton contactMenuButton = new MenuButton();

    private Transaction.Builder builder = new Transaction.Builder();

    private final TreeSet<TransactionType> typeSuggestions = new TreeSet<>();
    private final TreeSet<Contact> contactSuggestions = new TreeSet<>();
    private final TreeSet<Account> debitedSuggestions = new TreeSet<>();
    private final TreeSet<Account> debitedSuggestionsAll = new TreeSet<>();
    private final TreeSet<Account> creditedSuggestions = new TreeSet<>();
    private final TreeSet<Account> creditedSuggestionsAll = new TreeSet<>();
    private final TreeSet<String> commentSuggestions = new TreeSet<>();

    private final ValidationSupport validation = new ValidationSupport();

    private final Validator<String> DECIMAL_VALIDATOR = (Control control, String value) -> {
        var invalid = false;
        try {
            new BigDecimal(value);
            updateRateAmount();
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    TransactionDialog(DataCache cache) {
        this.cache = cache;

        setupDatePicker();
        datePicker.setValue(lastSelectedDate);

        getDialogPane().setContent(
            vBox(
                DOUBLE_SPACING,
                hBox(BIG_SPACING,
                    vBox(SMALL_SPACING, label(fxString(RB, "Date")), datePicker),
                    vBox(SMALL_SPACING,
                        label(fxString(RB, "Type")),
                        hBox(List.of(typeEdit, typeMenuButton, checkedCheckBox), hBox -> {
                            hBox.setSpacing(BIG_SPACING);
                            hBox.setAlignment(Pos.CENTER);
                        })
                    )
                ),
                hBox(BIG_SPACING,
                    vBox(SMALL_SPACING, label(fxString(RB, "Sum")), sumEdit),
                    vBox(SMALL_SPACING,
                        label(fxString(RB, "Rate")),
                        new HBox(rate1Edit, rateDir1Combo),
                        rateAmoutLabel)
                ),
                fxNode(
                    vBox(SMALL_SPACING,
                        label(fxString(RB, "column.Account.Debited")),
                        hBox(0,
                            fxNode(debitedAccountEdit, hBoxHGrow(ALWAYS)),
                            debitedMenuButton),
                        debitedCategoryLabel),
                    hBoxHGrow(ALWAYS)
                ),
                fxNode(
                    vBox(SMALL_SPACING,
                        label(fxString(RB, "column.Account.Credited")),
                        hBox(0,
                            fxNode(creditedAccountEdit, hBoxHGrow(ALWAYS)),
                            creditedMenuButton
                        ),
                        creditedCategoryLabel),
                    hBoxHGrow(ALWAYS)
                ),
                fxNode(
                    vBox(SMALL_SPACING,
                        label(fxString(RB, "Counterparty")),
                        hBox(0,
                            fxNode(contactEdit, hBoxHGrow(ALWAYS)),
                            contactMenuButton)),
                    hBoxHGrow(ALWAYS)
                ),
                fxNode(
                    vBox(SMALL_SPACING, label(fxString(RB, "Comment")), commentEdit),
                    hBoxHGrow(ALWAYS)
                ),
                vBox(SMALL_SPACING, label(fxString(RB, "Invoice")), invoiceNumberEdit)
            )
        );

        typeMenuButton.setFocusTraversable(false);
        checkedCheckBox.setFocusTraversable(false);
        debitedMenuButton.setFocusTraversable(false);
        creditedMenuButton.setFocusTraversable(false);
        contactMenuButton.setFocusTraversable(false);

        debitedAccountEdit.setPrefColumnCount(40);

        rate1Edit.setDisable(true);
        rate1Edit.setPrefColumnCount(5);

        rateDir1Combo.setDisable(true);

        rateAmoutLabel.getStyleClass().add(Styles.RATE_LABEL);
        debitedCategoryLabel.getStyleClass().add(Styles.SUB_LABEL);
        creditedCategoryLabel.getStyleClass().add(Styles.SUB_LABEL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        clearTitle();

        rateDir1Combo.getItems().setAll("/", "*");

        TextFields.bindAutoCompletion(typeEdit,
            new TransactionTypeCompletionProvider(typeSuggestions), TRANSACTION_TYPE_TO_STRING);
        TextFields.bindAutoCompletion(debitedAccountEdit,
            new CompletionProvider<>(debitedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(creditedAccountEdit,
            new CompletionProvider<>(creditedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(contactEdit, new CompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);
        TextFields.bindAutoCompletion(commentEdit, new StringCompletionProvider(commentSuggestions));

        creditedAccountEdit.focusedProperty().addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                processAutoFill();
            }
        });

        typeEdit.focusedProperty().addListener((x, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                handleTypeFocusLoss();
            }
        });

        onChangedTransactionTypes();
        setupAccountMenus();
        setupContactMenu();
        setupComments();

        createDefaultButtons(RB);

        var okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!buildTransaction()) {
                event.consume();
            }
            if (uuid == null) {
                // If new transaction remember date
                lastSelectedDate = datePicker.getValue();
            }
        });

        setResultConverter((ButtonType b) -> b == ButtonType.OK ? builder : null);

        runLater(() -> {
            createValidationSupport();
            typeEdit.requestFocus();
        });
    }

    TransactionDialog(Transaction transaction, DataCache cache) {
        this(cache);

        uuid = transaction.uuid();

        builder = new Transaction.Builder(transaction);

        setTitle(RB.getString("Transaction") + ": " + uuid);

        // Type
        typeEdit.setText(transaction.type().toString());

        // Accounts
        Optional<Account> accCredited = cache.getAccount(transaction.accountCreditedUuid());
        creditedAccountEdit.setText(accCredited.map(Account::name).orElse(""));

        Optional<Account> accDebited = cache.getAccount(transaction.accountDebitedUuid());
        debitedAccountEdit.setText(accDebited.map(Account::name).orElse(""));

        contactEdit.setText(cache.getContact(transaction.contactUuid()).map(Contact::name).orElse(""));

        // Other fields
        commentEdit.setText(transaction.comment());
        checkedCheckBox.setSelected(transaction.checked());
        invoiceNumberEdit.setText(transaction.invoiceNumber());

        // Rate
        var debitedCurrencyUuid = accDebited.map(Account::currencyUuid).orElse(null);
        var creditedCurrencyUuid = accCredited.map(Account::currencyUuid).orElse(null);

        if (Objects.equals(debitedCurrencyUuid, creditedCurrencyUuid)) {
            rate1Edit.setDisable(true);
            rate1Edit.setText("");
        } else {
            rate1Edit.setDisable(false);

            BigDecimal rate = transaction.rate();
            if (BigDecimal.ZERO.compareTo(rate) == 0) {
                rate = BigDecimal.ONE.setScale(FIELD_SCALE, RoundingMode.HALF_UP);
            }
            if (rate != null) {
                rate1Edit.setText(rate.toString());
                rateDir1Combo.getSelectionModel().select(transaction.rateDirection());
            } else {
                rate1Edit.setText("");
            }
        }

        // Day
        var trDate = LocalDate.of(transaction.year(), transaction.month(), transaction.day());
        datePicker.setValue(trDate);

        // Sum
        sumEdit.setText(transaction.amount().setScale(2, RoundingMode.HALF_UP).toString());
        updateRateAmount();
    }

    TransactionDialog(StatementRecord record, Account account, DataCache cache) {
        this(cache);

        datePicker.setValue(record.getActual());

        var amount = record.getAmountDecimal().orElse(BigDecimal.ZERO);
        sumEdit.setText(amount.abs().setScale(2, RoundingMode.HALF_UP).toString());

        var accountString = account == null ? "" : account.name();
        if (amount.signum() <= 0) {
            debitedAccountEdit.setText(accountString);
        } else {
            creditedAccountEdit.setText(accountString);
        }
    }

    private void setupDatePicker() {
        var tomorrowKey = new KeyCodeCombination(RIGHT, SHORTCUT_DOWN, ALT_DOWN);
        var yesterdayKey = new KeyCodeCombination(LEFT, SHORTCUT_DOWN, ALT_DOWN);
        var todayKey = new KeyCodeCombination(UP, SHORTCUT_DOWN, ALT_DOWN);
        var nextMonthKey = new KeyCodeCombination(RIGHT, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN);
        var prevMonthKey = new KeyCodeCombination(LEFT, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN);

        var tooltipText = String.format(RB.getString("datePicker.tooltip"),
            tomorrowKey.getDisplayText(),
            yesterdayKey.getDisplayText(),
            nextMonthKey.getDisplayText(),
            prevMonthKey.getDisplayText(),
            todayKey.getDisplayText());

        datePicker.setTooltip(new Tooltip(tooltipText));

        var accelerators = getDialogPane().getScene().getAccelerators();
        accelerators.put(tomorrowKey, this::tomorrow);
        accelerators.put(yesterdayKey, this::yesterday);
        accelerators.put(todayKey, this::today);
        accelerators.put(nextMonthKey, this::nextMonth);
        accelerators.put(prevMonthKey, this::prevMonth);

        datePicker.setEditable(false);
        datePicker.getEditor().prefColumnCountProperty().set(10);
    }

    private void clearTitle() {
        setTitle(RB.getString("Transaction"));
    }

    private void setupBanksAndCashMenuItems() {
        var banksAndCashAll = cache.getAccountsByType(CategoryType.BANKS_AND_CASH);
        var enabledCount = banksAndCashAll.stream().filter(Account::enabled).count();

        banksAndCashAll.stream()
            .sorted((a1, a2) -> a1.name().compareToIgnoreCase(a2.name()))
            .forEach(acc -> {
                debitedSuggestionsAll.add(acc);
                creditedSuggestionsAll.add(acc);

                if (acc.enabled()) {
                    var title = "[" + acc.name() + "]";

                    debitedMenuButton.getItems().add(menuItem(title, event -> onDebitedAccountSelected(acc)));
                    creditedMenuButton.getItems().add(menuItem(title, event -> onCreditedAccountSelected(acc)));

                    debitedSuggestions.add(acc);
                    creditedSuggestions.add(acc);
                }
            });

        if (enabledCount != 0) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    private void setupDebtMenuItems() {
        setAccountMenuItemsByCategory(CategoryType.DEBTS, "!");
    }

    private void setupAssetsMenuItems() {
        setAccountMenuItemsByCategory(CategoryType.ASSETS, ".");
    }

    private void setupPortfolioMenuItems() {
        setAccountMenuItemsByCategory(CategoryType.PORTFOLIO, "~");
    }

    private void setAccountMenuItemsByCategory(CategoryType categoryType, String prefix) {
        var categories = cache.getCategoriesByType(categoryType);

        categories.stream()
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .forEach(x -> {
                List<Account> accounts = cache.getAccountsByCategory(x.uuid());

                if (!accounts.isEmpty()) {
                    debitedMenuButton.getItems().add(new MenuItem(x.name()));
                    creditedMenuButton.getItems().add(new MenuItem(x.name()));

                    for (var acc : accounts) {
                        debitedSuggestionsAll.add(acc);
                        creditedSuggestionsAll.add(acc);

                        if (acc.enabled()) {
                            var title = "  " + prefix + " " + acc.name();

                            debitedMenuButton.getItems().add(
                                menuItem(title, event -> onDebitedAccountSelected(acc)));
                            creditedMenuButton.getItems().add(
                                menuItem(title, event -> onCreditedAccountSelected(acc)));

                            debitedSuggestions.add(acc);
                            creditedSuggestions.add(acc);
                        }
                    }
                }
            });

        if (!categories.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    private void onContactSelected(Contact c) {
        contactEdit.setText(c.name());
        builder.contactUuid(c.uuid());
    }

    private void onDebitedAccountSelected(Account acc) {
        debitedAccountEdit.setText(acc.name());
        enableDisableRate();
    }

    private void onCreditedAccountSelected(Account acc) {
        creditedAccountEdit.setText(acc.name());
        enableDisableRate();
    }

    private void onTransactionTypeSelected(TransactionType type) {
        typeEdit.setText(type.toString());
    }

    private boolean buildTransaction() {
        // Check type id
        handleTypeFocusLoss();
        Optional<TransactionType> type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
        type.ifPresent(it -> builder.type(it));

        var debitedAccount = checkTextFieldValue(debitedAccountEdit, debitedSuggestionsAll, ACCOUNT_TO_STRING);
        if (debitedAccount.isPresent()) {
            builder.accountDebitedUuid(debitedAccount.get().uuid());
            builder.accountDebitedCategoryUuid(debitedAccount.get().categoryUuid());
            builder.accountDebitedType(debitedAccount.get().type());
        } else {
            return false;
        }

        var creditedAccount = checkTextFieldValue(creditedAccountEdit, creditedSuggestionsAll, ACCOUNT_TO_STRING);
        if (creditedAccount.isPresent()) {
            builder.accountCreditedUuid(creditedAccount.get().uuid());
            builder.accountCreditedCategoryUuid(creditedAccount.get().categoryUuid());
            builder.accountCreditedType(creditedAccount.get().type());
        } else {
            return false;
        }

        // builder.day(daySpinner.getValue());
        builder.comment(commentEdit.getText());
        builder.checked(checkedCheckBox.isSelected());
        builder.invoiceNumber(invoiceNumberEdit.getText());

        try {
            builder.day(datePicker.getValue().getDayOfMonth());
            builder.month(datePicker.getValue().getMonthValue());
            builder.year(datePicker.getValue().getYear());

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

        var contactName = contactEdit.getText();
        if (contactName == null || contactName.isEmpty()) {
            builder.contactUuid(null);
        } else {
            var contact = checkTextFieldValue(contactName, contactSuggestions, CONTACT_TO_STRING);
            if (contact.isPresent()) {
                builder.contactUuid(contact.get().uuid());
            } else {
                builder.newContactName(contactName);
            }
        }

        builder.modified(System.currentTimeMillis());
        if (uuid != null) {
            builder.guid(uuid);
        }
        return true;
    }

    private void enableDisableRate() {
        boolean disable;

        if (builder.getAccountCreditedUuid() == null || builder.getAccountDebitedUuid() == null) {
            disable = true;
        } else {
            var c1 = cache.getAccount(builder.getAccountDebitedUuid())
                .map(Account::currencyUuid)
                .orElse(null);
            var c2 = cache.getAccount(builder.getAccountCreditedUuid())
                .map(Account::currencyUuid)
                .orElse(null);

            disable = Objects.equals(c1, c2);
        }

        rate1Edit.setDisable(disable);
        rateDir1Combo.setDisable(disable);

        if (!disable && rate1Edit.getText().isEmpty()) {
            rate1Edit.setText("1");
            rateDir1Combo.getSelectionModel().select(0);
        }
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value,
                                                              Collection<T> items,
                                                              StringConverter<T> converter)
    {
        return items.stream().filter(it -> converter.toString(it).equals(value)).findFirst();
    }

    private Optional<TransactionType> checkTransactionTypeFieldValue(String value, Collection<TransactionType> items) {
        return items.stream()
            .filter(it -> TRANSACTION_TYPE_TO_STRING.toString(it).equals(value)).findFirst();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(TextField field,
                                                              Collection<T> items,
                                                              StringConverter<T> converter)
    {
        return checkTextFieldValue(field.getText(), items, converter);
    }

    private Optional<TransactionType> checkTransactionTypeFieldValue(TextField field,
                                                                     Collection<TransactionType> items)
    {
        return checkTransactionTypeFieldValue(field.getText(), items);
    }

    private void handleTypeFocusLoss() {
        var type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
        if (type.isEmpty()) {
            typeEdit.setText(TransactionType.UNDEFINED.toString());
        }
    }

    private void createValidationSupport() {
        validation.registerValidator(typeEdit, (Control control, String value) -> {
            var type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
            return ValidationResult.fromErrorIf(control, null, type.isEmpty());
        });

        validation.registerValidator(debitedAccountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(debitedAccountEdit, debitedSuggestionsAll, ACCOUNT_TO_STRING);
            updateCategoryLabel(debitedCategoryLabel, account.orElse(null));

            builder.accountDebitedUuid(account.map(Account::uuid).orElse(null));

            enableDisableRate();
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(creditedAccountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(creditedAccountEdit, creditedSuggestionsAll, ACCOUNT_TO_STRING);
            updateCategoryLabel(creditedCategoryLabel, account.orElse(null));

            builder.accountCreditedUuid(account.map(Account::uuid).orElse(null));

            enableDisableRate();
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(sumEdit, DECIMAL_VALIDATOR);
        validation.registerValidator(rate1Edit, false, DECIMAL_VALIDATOR);

        validation.initInitialDecoration();
    }

    private void setupContactMenu() {
        contactMenuButton.getItems().clear();
        contactSuggestions.clear();

        cache.getContacts().stream()
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .forEach(x -> {
                contactMenuButton.getItems().add(menuItem(x.name(), event -> onContactSelected(x)));
                contactSuggestions.add(x);
            });

        contactMenuButton.setDisable(contactMenuButton.getItems().isEmpty());
    }

    private void onChangedTransactionTypes() {
        typeMenuButton.getItems().clear();
        typeSuggestions.clear();

        TransactionType.valuesAsList().forEach(x -> {
            if (x.isSeparator()) {
                typeMenuButton.getItems().add(new SeparatorMenuItem());
            } else {
                typeMenuButton.getItems().add(
                    menuItem(x.toString(), event -> onTransactionTypeSelected(x)));
                typeSuggestions.add(x);
            }
        });
    }

    private void setupAccountMenus() {
        debitedMenuButton.getItems().clear();
        creditedMenuButton.getItems().clear();
        debitedSuggestionsAll.clear();
        debitedSuggestions.clear();
        creditedSuggestionsAll.clear();
        creditedSuggestions.clear();

        // Bank and cash accounts first
        setupBanksAndCashMenuItems();

        // Incomes to debitable accounts
        List<Category> incomeCategories = cache.getCategoriesByType(CategoryType.INCOMES);
        incomeCategories.stream()
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .forEach(x -> {
                var accounts = cache.getAccountsByCategory(x.uuid());

                if (!accounts.isEmpty()) {
                    debitedMenuButton.getItems().add(new MenuItem(x.name()));

                    accounts.forEach(acc -> {
                        debitedMenuButton.getItems().add(
                            menuItem("  + " + acc.name(), events -> onDebitedAccountSelected(acc)));
                        debitedSuggestionsAll.add(acc);
                        debitedSuggestions.add(acc);
                    });
                }
            });

        if (!incomeCategories.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
        }

        // Expenses to creditable accounts
        List<Category> expenseCategories = cache.getCategoriesByType(CategoryType.EXPENSES);
        expenseCategories.stream()
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .forEach(x -> {
                var accounts = cache.getAccountsByCategory(x.uuid());

                if (!accounts.isEmpty()) {
                    creditedMenuButton.getItems().add(new MenuItem(x.name()));

                    accounts.forEach(acc -> {
                        creditedSuggestionsAll.add(acc);
                        creditedSuggestions.add(acc);
                        creditedMenuButton.getItems().add(
                            menuItem("  - " + acc.name(), event -> onCreditedAccountSelected(acc)));
                    });
                }
            });

        if (!expenseCategories.isEmpty()) {
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }

        setupDebtMenuItems();
        setupAssetsMenuItems();
        setupPortfolioMenuItems();
    }

    private void setupComments() {
        commentSuggestions.clear();
        commentSuggestions.addAll(cache.getUniqueTransactionComments());
    }

    private void updateRateAmount() {
        var amount = sumEdit.getText();
        if (amount.isEmpty()) {
            amount = "0";
        }

        var amountValue = new BigDecimal(amount).setScale(FIELD_SCALE, RoundingMode.HALF_UP);

        var rate = rate1Edit.getText();
        if (rate.isEmpty()) {
            rate = "1";
        }

        var rateValue = new BigDecimal(rate).setScale(FIELD_SCALE, RoundingMode.HALF_UP);

        BigDecimal total;

        if (rateDir1Combo.getSelectionModel().getSelectedIndex() == 0) {
            total = amountValue.divide(rateValue, RoundingMode.HALF_UP);
        } else {
            total = amountValue.multiply(rateValue);
        }

        runLater(() ->
            rateAmoutLabel.setText("= " + total.setScale(2, RoundingMode.HALF_UP).toString()));
    }

    private void updateCategoryLabel(Label label, Account account) {
        if (account != null) {
            var catName = cache.getCategory(account.categoryUuid()).map(Category::name).orElse("");
            label.setText(account.type() + " | " + catName);
        } else {
            label.setText("");
        }
    }

    private void processAutoFill() {
        var accDebitedUuid = builder.getAccountDebitedUuid();
        var accCreditedUuid = builder.getAccountCreditedUuid();

        if (accDebitedUuid != null && accCreditedUuid != null) {
            cache.getTransactions().stream()
                .filter(it -> Objects.equals(it.accountCreditedUuid(), accCreditedUuid)
                    && Objects.equals(it.accountDebitedUuid(), accDebitedUuid))
                .max(MoneyDAO.COMPARE_TRANSACTION_BY_DATE)
                .ifPresent(it -> {
                    if (commentEdit.getText().isEmpty()) {
                        commentEdit.setText(it.comment());
                    }
                    if (sumEdit.getText().isEmpty()) {
                        sumEdit.setText(it.amount().setScale(2, RoundingMode.HALF_UP).toString());
                    }

                    cache.getContact(it.contactUuid()).ifPresent(contact -> {
                        if (contactEdit.getText().isEmpty()) {
                            contactEdit.setText(contact.name());
                        }
                    });
                });
        }
    }

    private void today() {
        datePicker.setValue(LocalDate.now());
    }

    private void tomorrow() {
        datePicker.setValue(datePicker.getValue().plusDays(1));
    }

    private void yesterday() {
        datePicker.setValue(datePicker.getValue().minusDays(1));
    }

    private void nextMonth() {
        datePicker.setValue(datePicker.getValue().plusMonths(1));
    }

    private void prevMonth() {
        datePicker.setValue(datePicker.getValue().minusMonths(1));
    }

    TextField getTypeEdit() {
        return typeEdit;
    }

    TextField getDebitedAccountEdit() {
        return debitedAccountEdit;
    }

    TextField getCreditedAccountEdit() {
        return creditedAccountEdit;
    }

    TextField getCommentEdit() {
        return commentEdit;
    }

    TextField getSumEdit() {
        return sumEdit;
    }

    TextField getContactEdit() {
        return contactEdit;
    }

    TextField getRate1Edit() {
        return rate1Edit;
    }

    CheckBox getCheckedCheckBox() {
        return checkedCheckBox;
    }

    DatePicker getDatePicker() {
        return datePicker;
    }
}
