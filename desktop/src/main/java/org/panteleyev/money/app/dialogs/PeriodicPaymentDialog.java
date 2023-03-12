/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.dialogs;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.util.NamedCompletionProvider;
import org.panteleyev.money.app.util.NamedToStringConverter;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Contact;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.PeriodicPayment;
import org.panteleyev.money.model.PeriodicPaymentType;
import org.panteleyev.money.model.RecurrenceType;
import org.panteleyev.money.persistence.DataCache;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.IntStream;

import static javafx.application.Platform.runLater;
import static javafx.scene.layout.Priority.ALWAYS;
import static org.controlsfx.control.textfield.TextFields.bindAutoCompletion;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.hBoxHGrow;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxNode;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.choicebox.ChoiceBoxBuilder.choiceBox;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.Styles.SMALL_SPACING;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CREDITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_DEBITED_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_PERIODIC_PAYMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COUNTERPARTY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_RECURRENCE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_SUM;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

public class PeriodicPaymentDialog extends BaseDialog<PeriodicPayment> {
    private static final NamedToStringConverter<Contact> CONTACT_TO_STRING = new NamedToStringConverter<>();
    private static final NamedToStringConverter<Account> ACCOUNT_TO_STRING = new NamedToStringConverter<>();

    private final DataCache cache;
    private final PeriodicPayment.Builder builder;

    private final TreeSet<Account> debitedSuggestions = new TreeSet<>();
    private final TreeSet<Account> creditedSuggestions = new TreeSet<>();
    private final TreeSet<Contact> contactSuggestions = new TreeSet<>();
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

    private final TextField nameEdit = new TextField();
    private final ChoiceBox<PeriodicPaymentType> paymentTypeBox = choiceBox(PeriodicPaymentType.values(),
            b -> b.withStringConverter(Bundles::translate));
    private final ChoiceBox<RecurrenceType> recurrenceTypeBox = choiceBox(RecurrenceType.values(),
            b -> b.withStringConverter(Bundles::translate)
                    .withHandler(event -> onRecurrenceSelected())
    );
    private final ComboBox<Integer> dayComboBox = comboBox(IntStream.range(1, 29).boxed().toList(), b -> {});
    private final ChoiceBox<Month> monthChoiceBox = choiceBox(Month.values(),
            b -> b.withStringConverter(Bundles::translate));
    private final TextField commentEdit = new TextField();
    private final TextField debitedAccountEdit = new TextField();
    private final TextField creditedAccountEdit = new TextField();
    private final TextField contactEdit = new TextField();
    private final TextField sumEdit = new TextField();
    private final MenuButton debitedMenuButton = new MenuButton();
    private final MenuButton creditedMenuButton = new MenuButton();
    private final MenuButton contactMenuButton = new MenuButton();

    public PeriodicPaymentDialog(Controller owner, URL css, PeriodicPayment periodicPayment, DataCache cache) {
        super(owner, css);
        this.cache = cache;

        setTitle(fxString(UI, I18N_MISC_PERIODIC_PAYMENT));

        getDialogPane().setContent(gridPane(List.of(
                gridRow(label(fxString(UI, I18N_WORD_ENTITY_NAME, COLON)), nameEdit),
                gridRow(label(fxString(UI, I18N_WORD_TYPE, COLON)), paymentTypeBox),
                gridRow(label(fxString(UI, I18N_WORD_RECURRENCE, COLON)),
                        hBox(SMALL_SPACING, recurrenceTypeBox, dayComboBox, monthChoiceBox)),
                gridRow(label(fxString(UI, I18N_WORD_SUM)), sumEdit),
                gridRow(label(fxString(UI, I18N_MISC_DEBITED_ACCOUNT, COLON)),
                        hBox(0, fxNode(debitedAccountEdit, hBoxHGrow(ALWAYS)), debitedMenuButton)),
                gridRow(label(fxString(UI, I18N_MISC_CREDITED_ACCOUNT, COLON)),
                        hBox(0, fxNode(creditedAccountEdit, hBoxHGrow(ALWAYS)), creditedMenuButton)),
                gridRow(label(fxString(UI, I18N_WORD_COUNTERPARTY, COLON)),
                        hBox(0, fxNode(contactEdit, hBoxHGrow(ALWAYS)), contactMenuButton)),
                gridRow(label(fxString(UI, I18N_WORD_COMMENT, COLON)), commentEdit)
        ), b -> b.withStyle(Styles.GRID_PANE)));

        nameEdit.setPrefColumnCount(20);
        debitedMenuButton.setFocusTraversable(false);
        creditedMenuButton.setFocusTraversable(false);
        contactMenuButton.setFocusTraversable(false);

        bindAutoCompletion(debitedAccountEdit,
                new NamedCompletionProvider<>(debitedSuggestions), ACCOUNT_TO_STRING);
        bindAutoCompletion(creditedAccountEdit,
                new NamedCompletionProvider<>(creditedSuggestions), ACCOUNT_TO_STRING);
        bindAutoCompletion(contactEdit, new NamedCompletionProvider<>(contactSuggestions), CONTACT_TO_STRING);

        setupAccountMenus();
        setupContactMenu();

        createDefaultButtons(UI);

        builder = new PeriodicPayment.Builder(periodicPayment);
        if (periodicPayment == null) {
            paymentTypeBox.getSelectionModel().selectFirst();
            recurrenceTypeBox.getSelectionModel().selectFirst();
            dayComboBox.setValue(LocalDate.now().getDayOfMonth());
            monthChoiceBox.setValue(LocalDate.now().getMonth());
        } else {
            nameEdit.setText(periodicPayment.name());
            paymentTypeBox.getSelectionModel().select(periodicPayment.paymentType());
            recurrenceTypeBox.getSelectionModel().select(periodicPayment.recurrenceType());
            dayComboBox.setValue(periodicPayment.dayOfMonth());
            monthChoiceBox.setValue(periodicPayment.month());
            sumEdit.setText(periodicPayment.amount().toString());
            creditedAccountEdit.setText(
                    cache.getAccount(periodicPayment.accountCreditedUuid()).map(Account::name).orElse("")
            );
            debitedAccountEdit.setText(
                    cache.getAccount(periodicPayment.accountDebitedUuid()).map(Account::name).orElse("")
            );
            commentEdit.setText(periodicPayment.comment());
            contactEdit.setText(cache.getContact(periodicPayment.contactUuid()).map(Contact::name).orElse(""));
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            var now = System.currentTimeMillis();
            builder.name(nameEdit.getText())
                    .amount(new BigDecimal(sumEdit.getText()))
                    .paymentType(paymentTypeBox.getValue())
                    .recurrenceType(recurrenceTypeBox.getValue())
                    .dayOfMonth(dayComboBox.getValue())
                    .month(monthChoiceBox.getValue())
                    .comment(commentEdit.getText())
                    .modified(now);

            if (periodicPayment == null) {
                builder.uuid(UUID.randomUUID())
                        .created(now);
            }
            return builder.build();
        });

        runLater(() -> {
            createValidationSupport();
            nameEdit.requestFocus();
        });
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit, (Control control, String value) ->
                ValidationResult.fromErrorIf(control, null, value.isEmpty()));

        validation.registerValidator(debitedAccountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(debitedAccountEdit, debitedSuggestions, ACCOUNT_TO_STRING);
            builder.accountDebitedUuid(account.map(Account::uuid).orElse(null));
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(creditedAccountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
            builder.accountCreditedUuid(account.map(Account::uuid).orElse(null));
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(contactEdit, (Control control, String value) -> {
            var contact = checkTextFieldValue(contactEdit, contactSuggestions, CONTACT_TO_STRING);
            builder.contactUuid(contact.map(Contact::uuid).orElse(null));
            return ValidationResult.fromErrorIf(control, null, contact.isEmpty());
        });

        validation.registerValidator(sumEdit, DECIMAL_VALIDATOR);

        validation.initInitialDecoration();
    }

    private void setupAccountMenus() {
        debitedMenuButton.getItems().clear();
        creditedMenuButton.getItems().clear();
        debitedSuggestions.clear();
        creditedSuggestions.clear();

        // Bank and cash accounts first
        setupBanksAndCashMenuItems();

        // Expenses to creditable accounts
        List<Category> expenseCategories = cache.getCategoriesByType(CategoryType.EXPENSES);
        expenseCategories.stream()
                .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
                .forEach(x -> {
                    var accounts = cache.getAccountsByCategory(x.uuid());

                    if (!accounts.isEmpty()) {
                        creditedMenuButton.getItems().add(new MenuItem(x.name()));

                        accounts.forEach(acc -> {
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

    private void setupBanksAndCashMenuItems() {
        var banksAndCashAll = cache.getAccountsByType(CategoryType.BANKS_AND_CASH).stream()
                .filter(Account::enabled)
                .toList();

        banksAndCashAll.stream()
                .sorted((a1, a2) -> a1.name().compareToIgnoreCase(a2.name()))
                .forEach(acc -> {
                    var title = "[" + acc.name() + "]";

                    debitedMenuButton.getItems().add(menuItem(title, event -> onDebitedAccountSelected(acc)));
                    creditedMenuButton.getItems().add(menuItem(title, event -> onCreditedAccountSelected(acc)));

                    debitedSuggestions.add(acc);
                    creditedSuggestions.add(acc);
                });

        if (!banksAndCashAll.isEmpty()) {
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
                    var accounts = cache.getAccountsByCategory(x.uuid()).stream()
                            .filter(Account::enabled)
                            .toList();

                    if (!accounts.isEmpty()) {
                        debitedMenuButton.getItems().add(new MenuItem(x.name()));
                        creditedMenuButton.getItems().add(new MenuItem(x.name()));

                        for (var acc : accounts) {
                            var title = "  " + prefix + " " + acc.name();

                            debitedMenuButton.getItems().add(
                                    menuItem(title, event -> onDebitedAccountSelected(acc)));
                            creditedMenuButton.getItems().add(
                                    menuItem(title, event -> onCreditedAccountSelected(acc)));

                            debitedSuggestions.add(acc);
                            creditedSuggestions.add(acc);
                        }
                    }
                });

        if (!categories.isEmpty()) {
            debitedMenuButton.getItems().add(new SeparatorMenuItem());
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
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

    private void onContactSelected(Contact c) {
        contactEdit.setText(c.name());
//        builder.contactUuid(c.uuid());
    }

    private void onDebitedAccountSelected(Account acc) {
        debitedAccountEdit.setText(acc.name());
    }

    private void onCreditedAccountSelected(Account acc) {
        creditedAccountEdit.setText(acc.name());
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value,
                                                              Collection<T> items,
                                                              StringConverter<T> converter) {
        return items.stream().filter(it -> converter.toString(it).equals(value)).findFirst();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(TextField field,
                                                              Collection<T> items,
                                                              StringConverter<T> converter) {
        return checkTextFieldValue(field.getText(), items, converter);
    }

    private void onRecurrenceSelected() {
        monthChoiceBox.setVisible(recurrenceTypeBox.getValue() == RecurrenceType.YEARLY);
    }
}
