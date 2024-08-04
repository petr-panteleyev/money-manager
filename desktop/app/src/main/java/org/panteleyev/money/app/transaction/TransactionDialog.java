/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.transaction;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.BaseCompletionProvider;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.ToStringConverter;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.app.util.NamedCompletionProvider;
import org.panteleyev.money.app.util.NamedToStringConverter;
import org.panteleyev.money.app.util.StringCompletionProvider;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.model.TransactionType;
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
import static javafx.scene.layout.Priority.ALWAYS;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.Bundles.translate;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_LEFT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_RIGHT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_SHIFT_LEFT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_SHIFT_RIGHT;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_UP;
import static org.panteleyev.money.app.Styles.BIG_SPACING;
import static org.panteleyev.money.app.Styles.DOUBLE_SPACING;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;

public final class TransactionDialog extends BaseDialog<Transaction.Builder> {
    private static final ToStringConverter<TransactionType> TRANSACTION_TYPE_TO_STRING =
            new ToStringConverter<>() {
                public String toString(TransactionType obj) {
                    return translate(obj);
                }
            };

    private static final NamedToStringConverter<Contact> CONTACT_TO_STRING = new NamedToStringConverter<>();
    private static final NamedToStringConverter<Account> ACCOUNT_TO_STRING = new NamedToStringConverter<>();

    private static class TransactionTypeCompletionProvider extends BaseCompletionProvider<TransactionType> {
        TransactionTypeCompletionProvider(Set<TransactionType> set) {
            super(set, () -> settings().getAutoCompleteLength());
        }


        public String getElementString(TransactionType element) {
            return translate(element);
        }
    }

    private final static class CardListCell extends ListCell<Card> {
        @Override
        public void updateItem(Card card, boolean empty) {
            super.updateItem(card, empty);
            setText(null);
            setGraphic(null);

            if (empty || card == null) {
                return;
            }

            setText(card.number());
            setGraphic(IconManager.getCardImageView(card));
        }
    }


    private final DataCache cache;

    private UUID uuid;      // current transaction uuid if any
    private Transaction initialTransaction = null;

    // Intermediate validated values
    private TransactionType transactionType = null;
    private Account debitedAccount = null;
    private Account creditedAccount = null;

    private final DatePicker datePicker = new DatePicker();

    private static LocalDate lastSelectedDate = LocalDate.now();

    private final TextField typeEdit = new TextField();
    private final TextField debitedAccountEdit = new TextField();
    private final TextField creditedAccountEdit = new TextField();
    private final TextField contactEdit = new TextField();
    private final TextField debitAmountEdit = new TextField();
    private final TextField creditAmountEdit = new TextField();
    private final CheckBox checkedCheckBox = new CheckBox();
    private final TextField commentEdit = new TextField();
    private final TextField invoiceNumberEdit = new TextField();
    private final Label debitedCategoryLabel = new Label();
    private final Label creditedCategoryLabel = new Label();
    private final DatePicker statementDatePicker = new DatePicker();

    private final MenuButton typeMenuButton = new MenuButton();
    private final MenuButton debitedMenuButton = new MenuButton();
    private final MenuButton creditedMenuButton = new MenuButton();
    private final MenuButton contactMenuButton = new MenuButton();

    private Transaction.Builder builder = new Transaction.Builder();

    private final Set<TransactionType> typeSuggestions = new TreeSet<>();
    private final Set<Contact> contactSuggestions = new TreeSet<>();
    private final Set<Account> debitedSuggestions = new TreeSet<>();
    private final Set<Account> debitedSuggestionsAll = new TreeSet<>();
    private final Set<Account> creditedSuggestions = new TreeSet<>();
    private final Set<Account> creditedSuggestionsAll = new TreeSet<>();
    private final Set<String> commentSuggestions = new TreeSet<>();
    private final ComboBox<Card> cardComboBox = new ComboBox<>();

    private final ValidationSupport validation = new ValidationSupport();

    private final Validator<String> DECIMAL_VALIDATOR = (Control control, String value) -> {
        var invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    TransactionDialog(Controller owner, String css, DataCache cache) {
        super(owner, css);

        this.cache = cache;

        setupDatePicker();
        setupCardComboBox();
        datePicker.setValue(lastSelectedDate);

        getDialogPane().setContent(
                vBox(
                        DOUBLE_SPACING,
                        hBox(BIG_SPACING,
                                vBox(SMALL_SPACING, label("Дата"),
                                        hBox(List.of(datePicker, checkedCheckBox), hBox -> {
                                            hBox.setSpacing(BIG_SPACING);
                                            hBox.setAlignment(Pos.CENTER);
                                        })
                                )
                        ),
                        hBox(BIG_SPACING,
                                vBox(SMALL_SPACING, label("Дебет"), debitAmountEdit),
                                vBox(SMALL_SPACING, label("Кредит"), creditAmountEdit)
                        ),
                        hBox(BIG_SPACING,
                                vBox(SMALL_SPACING,
                                        label("Тип"),
                                        hBox(List.of(typeEdit, typeMenuButton), hBox -> {
                                            hBox.setSpacing(BIG_SPACING);
                                            hBox.setAlignment(Pos.CENTER);
                                        })
                                )
                        ),
                        fxNode(
                                vBox(SMALL_SPACING,
                                        label("Исходный счет"),
                                        hBox(0,
                                                fxNode(debitedAccountEdit, hBoxHGrow(ALWAYS)),
                                                debitedMenuButton),
                                        debitedCategoryLabel),
                                hBoxHGrow(ALWAYS)
                        ),
                        fxNode(
                                vBox(SMALL_SPACING,
                                        label("Карта"),
                                        hBox(0,
                                                fxNode(cardComboBox, hBoxHGrow(ALWAYS))
                                        ), label("")
                                ),
                                hBoxHGrow(ALWAYS)
                        ),
                        fxNode(
                                vBox(SMALL_SPACING,
                                        label("Счет получателя"),
                                        hBox(0,
                                                fxNode(creditedAccountEdit, hBoxHGrow(ALWAYS)),
                                                creditedMenuButton
                                        ),
                                        creditedCategoryLabel),
                                hBoxHGrow(ALWAYS)
                        ),
                        fxNode(
                                vBox(SMALL_SPACING,
                                        label("Контрагент"),
                                        hBox(0,
                                                fxNode(contactEdit, hBoxHGrow(ALWAYS)),
                                                contactMenuButton)),
                                hBoxHGrow(ALWAYS)
                        ),
                        fxNode(
                                vBox(SMALL_SPACING, label("Комментарий"), commentEdit),
                                hBoxHGrow(ALWAYS)
                        ),
                        vBox(SMALL_SPACING, label("Счёт"), invoiceNumberEdit),
                        vBox(SMALL_SPACING, label("Дата по выписке"), statementDatePicker)
                )
        );

        typeMenuButton.setFocusTraversable(false);
        checkedCheckBox.setFocusTraversable(false);
        debitedMenuButton.setFocusTraversable(false);
        creditedMenuButton.setFocusTraversable(false);
        contactMenuButton.setFocusTraversable(false);

        debitedAccountEdit.setPrefColumnCount(40);
        creditAmountEdit.setDisable(true);

        debitedCategoryLabel.getStyleClass().add(Styles.SUB_LABEL);
        creditedCategoryLabel.getStyleClass().add(Styles.SUB_LABEL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        clearTitle();

        TextFields.bindAutoCompletion(typeEdit,
                new TransactionTypeCompletionProvider(typeSuggestions), TRANSACTION_TYPE_TO_STRING);
        TextFields.bindAutoCompletion(debitedAccountEdit,
                new NamedCompletionProvider<>(debitedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(creditedAccountEdit,
                new NamedCompletionProvider<>(creditedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(contactEdit, new NamedCompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);
        TextFields.bindAutoCompletion(commentEdit, new StringCompletionProvider(commentSuggestions));

        creditedAccountEdit.focusedProperty().addListener((_, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                processAutoFill();
            }
        });

        typeEdit.focusedProperty().addListener((_, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                handleTypeFocusLoss();
            }
        });

        debitAmountEdit.focusedProperty().addListener((_, oldValue, newValue) -> {
            if (oldValue && !newValue) {
                onDebitAmountEditFocusLoss();
            }
        });

        onChangedTransactionTypes();
        setupAccountMenus();
        setupContactMenu();
        setupComments();

        createDefaultButtons(UI);

        var okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(validation.invalidProperty());

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
            debitAmountEdit.requestFocus();
        });
    }

    TransactionDialog(Controller owner, String css, Transaction transaction, DataCache cache) {
        this(owner, css, cache);

        uuid = transaction.uuid();
        initialTransaction = transaction;

        builder = new Transaction.Builder(transaction);

        setTitle("Проводка: " + uuid);

        // Type
        typeEdit.setText(translate(transaction.type()));
        transactionType = transaction.type();

        // Accounts
        Optional<Account> accCredited = cache.getAccount(transaction.accountCreditedUuid());
        creditedAccount = accCredited.orElse(null);
        creditedAccountEdit.setText(accCredited.map(Account::name).orElse(""));

        Optional<Account> accDebited = cache.getAccount(transaction.accountDebitedUuid());
        debitedAccount = accDebited.orElse(null);
        debitedAccountEdit.setText(accDebited.map(Account::name).orElse(""));

        updateCardComboBox(true);

        contactEdit.setText(cache.getContact(transaction.contactUuid()).map(Contact::name).orElse(""));

        // Other fields
        commentEdit.setText(transaction.comment());
        checkedCheckBox.setSelected(transaction.checked());
        invoiceNumberEdit.setText(transaction.invoiceNumber());

        // Rate
        var debitedCurrencyUuid = accDebited.map(Account::currencyUuid).orElse(null);
        var creditedCurrencyUuid = accCredited.map(Account::currencyUuid).orElse(null);

        creditAmountEdit.setDisable(Objects.equals(debitedCurrencyUuid, creditedCurrencyUuid));

        // Day
        datePicker.setValue(transaction.transactionDate());

        statementDatePicker.setValue(transaction.statementDate());

        // Sum
        debitAmountEdit.setText(transaction.amount().setScale(2, RoundingMode.HALF_UP).toString());
        creditAmountEdit.setText(transaction.creditAmount().setScale(2, RoundingMode.HALF_UP).toString());
    }

    public TransactionDialog(Controller owner, String css, StatementRecord record, Account account, DataCache cache) {
        this(owner, css, cache);

        datePicker.setValue(record.getActual());

        var amount = record.getAmountDecimal().orElse(BigDecimal.ZERO);
        debitAmountEdit.setText(amount.abs().setScale(2, RoundingMode.HALF_UP).toString());

        var accountString = account == null ? "" : account.name();
        if (amount.signum() <= 0) {
            debitedAccountEdit.setText(accountString);
        } else {
            creditedAccountEdit.setText(accountString);
        }
    }

    private void setupDatePicker() {
        var tomorrowKey = SHORTCUT_ALT_RIGHT;
        var yesterdayKey = SHORTCUT_ALT_LEFT;
        var todayKey = SHORTCUT_ALT_UP;
        var nextMonthKey = SHORTCUT_ALT_SHIFT_RIGHT;
        var prevMonthKey = SHORTCUT_ALT_SHIFT_LEFT;

        var tooltipText = String.format("""
                        %s - следующий день
                        %s - предыдущий день
                        %s - следующий месяц
                        %s - предыдущий месяц
                        %s - сегодня""",
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

    private void setupCardComboBox() {
        cardComboBox.setEditable(false);
        cardComboBox.setCellFactory(_ -> new CardListCell());
        cardComboBox.setButtonCell(new CardListCell());
    }

    private void updateCardComboBox(boolean initial) {
        if (transactionType != TransactionType.CARD_PAYMENT || debitedAccount == null) {
            cardComboBox.getItems().clear();
        } else {
            var selected = cardComboBox.getSelectionModel().getSelectedItem();
            var accountCards = cache.getCardsByAccount(debitedAccount);
            cardComboBox.getItems().setAll(accountCards);
            if (accountCards.contains(selected)) {
                cardComboBox.getSelectionModel().select(selected);
            }

            if (!accountCards.isEmpty() && initial) {
                cardComboBox.getSelectionModel().clearSelection();
                cache.getCard(initialTransaction.cardUuid()).ifPresent(card -> {
                            cardComboBox.getSelectionModel().select(card);
                        }
                );
            }
        }
        runLater(() -> validation.revalidate(cardComboBox));
    }

    private void clearTitle() {
        setTitle("Проводка");
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

                        debitedMenuButton.getItems().add(menuItem(title, _ -> onDebitedAccountSelected(acc)));
                        creditedMenuButton.getItems().add(menuItem(title, _ -> onCreditedAccountSelected(acc)));

                        debitedSuggestions.add(acc);
                        creditedSuggestions.add(acc);

                        var accCards = cache.getCardsByAccount(acc);
                        for (var card : accCards) {
                            var cardAlias = new Account.Builder(acc)
                                    .name(card.number())
                                    .build();
                            debitedSuggestions.add(cardAlias);
                            debitedSuggestionsAll.add(cardAlias);
                        }
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
                                        menuItem(title, _ -> onDebitedAccountSelected(acc)));
                                creditedMenuButton.getItems().add(
                                        menuItem(title, _ -> onCreditedAccountSelected(acc)));

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
        typeEdit.setText(translate(type));
    }

    private boolean buildTransaction() {
        // Check type id
        handleTypeFocusLoss();

        debitedAccount = checkDebitedAccount().orElse(null);
        creditedAccount = checkCreditedAccount().orElse(null);

        Optional<TransactionType> type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
        type.ifPresent(it -> builder.type(it));

        if (debitedAccount != null) {
            builder.accountDebitedUuid(debitedAccount.uuid());
            builder.accountDebitedCategoryUuid(debitedAccount.categoryUuid());
            builder.accountDebitedType(debitedAccount.type());
        } else {
            return false;
        }

        if (creditedAccount != null) {
            builder.accountCreditedUuid(creditedAccount.uuid());
            builder.accountCreditedCategoryUuid(creditedAccount.categoryUuid());
            builder.accountCreditedType(creditedAccount.type());
        } else {
            return false;
        }

        builder.comment(commentEdit.getText())
                .checked(checkedCheckBox.isSelected())
                .invoiceNumber(invoiceNumberEdit.getText())
                .statementDate(statementDatePicker.getValue());

        var card = cardComboBox.getValue();
        builder.cardUuid(card == null ? null : card.uuid());

        try {
            builder.transactionDate(datePicker.getValue());

            builder.amount(new BigDecimal(debitAmountEdit.getText()));
            builder.creditAmount(new BigDecimal(creditAmountEdit.getText()));
        } catch (NumberFormatException ex) {
            return false;
        }

        var contactName = contactEdit.getText();
        if (contactName == null || contactName.isEmpty()) {
            builder.contactUuid(null);
        } else {
            checkContact(contactName).ifPresentOrElse(contact -> builder.contactUuid(contact.uuid()),
                    () -> builder.newContactName(contactName));
        }

        builder.modified(System.currentTimeMillis());
        if (uuid != null) {
            builder.uuid(uuid);
        }
        return true;
    }

    private void enableDisableRate() {
        boolean disable;

        if (creditedAccount == null || debitedAccount == null) {
            disable = true;
        } else {
            disable = Objects.equals(debitedAccount.currencyUuid(), creditedAccount.currencyUuid());
        }

        creditAmountEdit.setDisable(disable);
        onDebitAmountEditFocusLoss();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value,
                                                              Collection<T> items,
                                                              StringConverter<T> converter) {
        return items.stream().filter(it -> converter.toString(it).equals(value)).findFirst();
    }

    private Optional<TransactionType> checkTransactionTypeFieldValue(String value, Collection<TransactionType> items) {
        return items.stream()
                .filter(it -> TRANSACTION_TYPE_TO_STRING.toString(it).equals(value)).findFirst();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(TextField field,
                                                              Collection<T> items,
                                                              StringConverter<T> converter) {
        return checkTextFieldValue(field.getText(), items, converter);
    }

    private Optional<TransactionType> checkTransactionTypeFieldValue(TextField field,
                                                                     Collection<TransactionType> items) {
        return checkTransactionTypeFieldValue(field.getText(), items);
    }

    private void handleTypeFocusLoss() {
        var type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
        if (type.isEmpty()) {
            typeEdit.setText(translate(TransactionType.UNDEFINED));
        }
    }

    private void createValidationSupport() {
        validation.registerValidator(typeEdit, (Control control, String _) -> {
            var type = checkTransactionTypeFieldValue(typeEdit, typeSuggestions);
            transactionType = type.orElse(TransactionType.UNDEFINED);
            updateCardComboBox(false);
            return ValidationResult.fromErrorIf(control, null, type.isEmpty());
        });

        validation.registerValidator(cardComboBox, (Control control, Card value) -> {
            var invalid = transactionType == TransactionType.CARD_PAYMENT && value == null;
            return ValidationResult.fromErrorIf(control, null, invalid);
        });

        validation.registerValidator(debitedAccountEdit, (Control control, String _) -> {
            var account = checkDebitedAccount();
            updateCategoryLabel(debitedCategoryLabel, account.orElse(null));

            debitedAccount = account.orElse(null);

            enableDisableRate();
            updateCardComboBox(false);
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(creditedAccountEdit, (Control control, String _) -> {
            var account = checkCreditedAccount();
            updateCategoryLabel(creditedCategoryLabel, account.orElse(null));

            creditedAccount = account.orElse(null);

            enableDisableRate();
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(debitAmountEdit, DECIMAL_VALIDATOR);
        validation.registerValidator(creditAmountEdit, DECIMAL_VALIDATOR);

        validation.initInitialDecoration();
    }

    private void setupContactMenu() {
        contactMenuButton.getItems().clear();
        contactSuggestions.clear();

        cache.getContacts().stream()
                .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
                .forEach(x -> {
                    contactMenuButton.getItems().add(menuItem(x.name(), _ -> onContactSelected(x)));
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
                        menuItem(translate(x), _ -> onTransactionTypeSelected(x)));
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
                                    menuItem("  + " + acc.name(), _ -> onDebitedAccountSelected(acc)));
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
                                    menuItem("  - " + acc.name(), _ -> onCreditedAccountSelected(acc)));
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

    private void updateCategoryLabel(Label label, Account account) {
        if (account == null) {
            label.setText("");
        } else {
            var catName = cache.getCategory(account.categoryUuid()).map(Category::name).orElse("");
            label.setText(translate(account.type()) + " | " + catName);
        }
    }

    private void processAutoFill() {
        if (debitedAccount == null || creditedAccount == null) {
            return;
        }

        var accDebitedUuid = debitedAccount.uuid();
        var accCreditedUuid = creditedAccount.uuid();

        cache.getTransactions().stream()
                .filter(it -> Objects.equals(it.accountCreditedUuid(), accCreditedUuid)
                        && Objects.equals(it.accountDebitedUuid(), accDebitedUuid))
                .max(Comparators.transactionsByDate())
                .ifPresent(it -> {
                    if (commentEdit.getText().isEmpty()) {
                        commentEdit.setText(it.comment());
                    }
                    if (debitAmountEdit.getText().isEmpty()) {
                        debitAmountEdit.setText(it.amount().setScale(2, RoundingMode.HALF_UP).toString());
                    }

                    cache.getContact(it.contactUuid()).ifPresent(contact -> {
                        if (contactEdit.getText().isEmpty()) {
                            contactEdit.setText(contact.name());
                        }
                    });
                });
    }

    private void onDebitAmountEditFocusLoss() {
        if (creditAmountEdit.getText().isEmpty() || creditAmountEdit.isDisable()) {
            creditAmountEdit.setText(debitAmountEdit.getText());
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

    private Optional<Account> checkDebitedAccount() {
        return checkTextFieldValue(debitedAccountEdit, debitedSuggestionsAll, ACCOUNT_TO_STRING);
    }

    private Optional<Account> checkCreditedAccount() {
        return checkTextFieldValue(creditedAccountEdit, creditedSuggestionsAll, ACCOUNT_TO_STRING);
    }

    private Optional<Contact> checkContact(String contactName) {
        return checkTextFieldValue(contactName, contactSuggestions, CONTACT_TO_STRING);
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

    TextField getDebitAmountEdit() {
        return debitAmountEdit;
    }

    TextField getCreditAmountEdit() {
        return creditAmountEdit;
    }

    TextField getContactEdit() {
        return contactEdit;
    }

    CheckBox getCheckedCheckBox() {
        return checkedCheckBox;
    }

    DatePicker getDatePicker() {
        return datePicker;
    }
}
