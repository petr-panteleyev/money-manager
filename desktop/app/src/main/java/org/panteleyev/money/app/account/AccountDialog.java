/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.BaseCompletionProvider;
import org.panteleyev.money.app.Bundles;
import org.panteleyev.money.app.MainWindowController;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.ToStringConverter;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.desktop.commons.DataCache;
import org.panteleyev.money.desktop.commons.ReadOnlyNamedConverter;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.panteleyev.fx.FxUtils.SKIP;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.icons.IconManager.EMPTY_ICON;

class AccountDialog extends BaseDialog<Account> {
    private record CurrencyOrSecurity(Object object) implements Comparable<CurrencyOrSecurity> {
        @Override
        public int compareTo(CurrencyOrSecurity other) {
            return this.object.toString().compareTo(other.toString());
        }

        @Override
        public String toString() {
            return switch (object) {
                case Currency currency -> currency.symbol();
                case ExchangeSecurity security -> String.format("%s (%s)", security.secId(), security.shortName());
                default -> "";
            };
        }
    }

    private static final class CurrencyCompletionProvider extends BaseCompletionProvider<CurrencyOrSecurity> {
        public CurrencyCompletionProvider(Set<CurrencyOrSecurity> set) {
            super(set, () -> settings().getAutoCompleteLength());
        }

        public String getElementString(CurrencyOrSecurity cos) {
            return cos.toString();
        }
    }

    private static final ToStringConverter<CurrencyOrSecurity> CURRENCY_TO_STRING =
            new ToStringConverter<>() {
                public String toString(CurrencyOrSecurity obj) {
                    return obj.toString();
                }
            };


    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final TextField initialEdit = new TextField();
    private final TextField creditEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final TextField accountNumberEdit = new TextField();
    private final ComboBox<CategoryType> typeComboBox = comboBox(CategoryType.values(),
            b -> b.withHandler(_ -> onCategoryTypeSelected())
                    .withStringConverter(Bundles::translate));
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final TextField currencyEdit = new TextField();
    private final MenuButton currencyMenuButton = new MenuButton();
    private final Set<CurrencyOrSecurity> currencySuggestions = new TreeSet<>();
    private final CheckBox activeCheckBox = new CheckBox("Активен");
    private final TextField interestEdit = new TextField();
    private final DatePicker closingDatePicker = new DatePicker();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();

    private final Collection<Category> categories;

    private final DataCache cache;

    AccountDialog(Controller owner, String css, Category initialCategory) {
        this(owner, css, null, initialCategory, cache());
    }

    AccountDialog(Controller owner, String css, Category initialCategory, DataCache cache) {
        this(owner, css, null, initialCategory, cache);
    }

    AccountDialog(Controller owner, String css, Account account, Category initialCategory) {
        this(owner, css, account, initialCategory, cache());
    }

    AccountDialog(Controller owner, String css, Account account, Category initialCategory, DataCache cache) {
        super(owner, css);
        setTitle("Счёт");

        this.cache = cache;

        getDialogPane().setContent(
                gridPane(
                        List.of(
                                gridRow(label("Название:"), gridCell(nameEdit, 2, 1)),
                                gridRow(label("Тип:"), typeComboBox, iconComboBox),
                                gridRow(label("Категория:"), gridCell(categoryComboBox, 2, 1)),
                                gridRow(label("Начальный баланс:"), gridCell(initialEdit, 2, 1)),
                                gridRow(label("Кредит:"), gridCell(creditEdit, 2, 1)),
                                gridRow(label("Номер счёта:"), gridCell(accountNumberEdit, 2, 1)),
                                gridRow(label("Комментарий:"), gridCell(commentEdit, 2, 1)),
                                gridRow(label("Валюта:"), currencyEdit, currencyMenuButton),
                                gridRow(label("Проценты:"), gridCell(interestEdit, 2, 1)),
                                gridRow(label("Дата закрытия:"), gridCell(closingDatePicker, 2, 1)),
                                gridRow(SKIP, gridCell(activeCheckBox, 2, 1))
                        ), b -> b.withStyle(Styles.GRID_PANE)
                )
        );

        nameEdit.setPrefColumnCount(20);

        categories = cache.getCategories().sorted(Category.COMPARE_BY_NAME);

        categoryComboBox.setConverter(new ReadOnlyNamedConverter<>());
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        IconManager.setupComboBox(iconComboBox);

        TextFields.bindAutoCompletion(currencyEdit, new CurrencyCompletionProvider(currencySuggestions), CURRENCY_TO_STRING);

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            creditEdit.setText("0.0");
            activeCheckBox.setSelected(true);
            interestEdit.setText("0.0");
            closingDatePicker.setValue(null);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);

            if (initialCategory != null) {
                typeComboBox.getSelectionModel().select(initialCategory.type());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                        .select(cache.getCategory(initialCategory.uuid()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }
        } else {
            nameEdit.setText(account.name());
            commentEdit.setText(account.comment());
            accountNumberEdit.setText(account.accountNumber());
            initialEdit.setText(account.openingBalance().toString());
            creditEdit.setText(account.accountLimit().toString());
            activeCheckBox.setSelected(account.enabled());
            interestEdit.setText(account.interest().toString());
            closingDatePicker.setValue(account.closingDate());
            iconComboBox.getSelectionModel().select(cache.getIcon(account.iconUuid()).orElse(EMPTY_ICON));

            typeComboBox.getSelectionModel().select(account.type());
            categoryComboBox.getSelectionModel()
                    .select(cache.getCategory(account.categoryUuid()).orElse(null));

            CurrencyOrSecurity currencyOrSecurity;
            if (account.currencyUuid() != null) {
                currencyOrSecurity = new CurrencyOrSecurity(cache.getCurrency(account.currencyUuid()).orElse(null));
            } else if (account.securityUuid() != null) {
                currencyOrSecurity = new CurrencyOrSecurity(cache.getExchangeSecurity(account.securityUuid()).orElse(null));
            } else {
                currencyOrSecurity = new CurrencyOrSecurity(null);
            }
            currencyEdit.setText(currencyOrSecurity.toString());
        }
        setupCurrencyMenuButton();

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            var now = System.currentTimeMillis();

            // Check for empty icon
            var icon = iconComboBox.getSelectionModel().getSelectedItem();
            var uconUuid = icon == EMPTY_ICON ? null : icon.uuid();

            var builder = new Account.Builder(account)
                    .name(nameEdit.getText())
                    .comment(commentEdit.getText())
                    .accountNumber(accountNumberEdit.getText())
                    .openingBalance(new BigDecimal(initialEdit.getText()))
                    .accountLimit(new BigDecimal(creditEdit.getText()))
                    .type(typeComboBox.getSelectionModel().getSelectedItem())
                    .categoryUuid(categoryComboBox.getSelectionModel().getSelectedItem().uuid())
                    .enabled(activeCheckBox.isSelected())
                    .interest(new BigDecimal(interestEdit.getText()))
                    .closingDate(closingDatePicker.getValue())
                    .iconUuid(uconUuid)
                    .modified(now);

            var selectedCurrencyOrSecurity = findCurrencyOrSecurity(currencyEdit.getText())
                    .orElseThrow();

            if (selectedCurrencyOrSecurity.object() instanceof Currency currency) {
                builder.currencyUuid(currency.uuid())
                        .securityUuid(null);
            } else if (selectedCurrencyOrSecurity.object() instanceof ExchangeSecurity security) {
                builder.currencyUuid(null)
                        .securityUuid(security.uuid());
            }

            if (account == null) {
                builder.uuid(UUID.randomUUID())
                        .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());

        Platform.runLater(this::createValidationSupport);
    }

    private void onCategoryTypeSelected() {
        var type = typeComboBox.getSelectionModel().getSelectedItem();

        var filtered = categories.stream()
                .filter(c -> c.type().equals(type))
                .toList();

        categoryComboBox.setItems(FXCollections.observableArrayList(filtered));

        if (!filtered.isEmpty()) {
            categoryComboBox.getSelectionModel().select(0);
        }

        setupCurrencyMenuButton();
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit,
                (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(categoryComboBox,
                (Control control, Category value) -> ValidationResult.fromErrorIf(control, null, value == null));
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(creditEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(interestEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(currencyEdit, (Control control, String value) -> {
            var correct = findCurrencyOrSecurity(value).isPresent();
            return ValidationResult.fromErrorIf(control, null, !correct);
        });
        validation.initInitialDecoration();
    }

    private void setupCurrencyMenuButton() {
        currencyMenuButton.getItems().clear();
        currencySuggestions.clear();

        // Add currencies
        for (var currency : cache.getCurrencies()) {
            var item = new CurrencyOrSecurity(currency);
            currencyMenuButton.getItems().add(
                    menuItem(item.toString(), _ -> onCurrencyOrSecuritySelected(item))
            );
            currencySuggestions.add(new CurrencyOrSecurity(currency));
        }

        if (typeComboBox.getValue() == CategoryType.PORTFOLIO) {
            // Add securities
            if (!cache.getExchangeSecurities().isEmpty()) {
                currencyMenuButton.getItems().add(new SeparatorMenuItem());
                for (var security : cache.getExchangeSecurities()) {
                    var item = new CurrencyOrSecurity(security);
                    currencySuggestions.add(item);
                    currencyMenuButton.getItems().add(
                            menuItem(item.toString(), _ -> onCurrencyOrSecuritySelected(item))
                    );
                }
            }
        }

        validation.revalidate(currencyEdit);
    }

    private void onCurrencyOrSecuritySelected(CurrencyOrSecurity cos) {
        currencyEdit.setText(cos.toString());
    }

    private Optional<CurrencyOrSecurity> findCurrencyOrSecurity(String text) {
        return currencySuggestions.stream()
                .filter(cos -> Objects.equals(cos.toString(), text))
                .findAny();
    }

    TextField getNameEdit() {
        return nameEdit;
    }

    TextField getCommentEdit() {
        return commentEdit;
    }

    TextField getAccountNumberEdit() {
        return accountNumberEdit;
    }

    TextField getInterestEdit() {
        return interestEdit;
    }

    TextField getCreditEdit() {
        return creditEdit;
    }

    ComboBox<CategoryType> getTypeComboBox() {
        return typeComboBox;
    }

    TextField getOpeningBalanceEdit() {
        return initialEdit;
    }

    TextField getCurrencyEdit() {
        return currencyEdit;
    }
}
