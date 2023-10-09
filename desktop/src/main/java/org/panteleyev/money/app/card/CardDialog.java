/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.card;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.Images;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.util.NamedCompletionProvider;
import org.panteleyev.money.app.util.NamedToStringConverter;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Card;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Named;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.MainWindowController.UI;

final class CardDialog extends BaseDialog<Card> {
    private static final NamedToStringConverter<Account> ACCOUNT_TO_STRING = new NamedToStringConverter<>();

    private final ValidationSupport validation = new ValidationSupport();

    private final TextField accountEdit = new TextField();

    private final TextField numberEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final CheckBox enabledCheckBox = new CheckBox("Активна");
    private final DatePicker expritationDatePicker = new DatePicker();
    private final ComboBox<CardType> typeComboBox = comboBox(CardType.values(),
            b -> b.withDefaultString("-")
                    .withImageConverter(Images::getCardTypeIcon));

    private final Set<Account> accountSuggestions = new TreeSet<>();
    private final Set<Account> accountSuggestionsAll = new TreeSet<>();

    private final MenuButton accountMenuButton = new MenuButton();

    private final Card.Builder builder;

    CardDialog(Controller owner, Card card) {
        super(owner, settings().getDialogCssFileUrl());
        setTitle("Карта");

        accountEdit.setPrefColumnCount(20);
        numberEdit.setPrefColumnCount(20);
        accountMenuButton.setFocusTraversable(false);

        setupAccountMenuItems();

        TextFields.bindAutoCompletion(accountEdit,
                new NamedCompletionProvider<>(accountSuggestions), ACCOUNT_TO_STRING);

        getDialogPane().setContent(gridPane(
                List.of(
                        gridRow(label("Счёт:"), accountEdit, accountMenuButton),
                        gridRow(label("Тип:"), gridCell(typeComboBox, 2, 1)),
                        gridRow(label("Номер:"), gridCell(numberEdit, 2, 1)),
                        gridRow(label("Exp:"), gridCell(expritationDatePicker, 2, 1)),
                        gridRow(label("Описание:"), gridCell(commentEdit, 2, 1)),
                        gridRow(label(""), gridCell(enabledCheckBox, 2, 1))
                ), b -> b.withStyle(Styles.GRID_PANE)
        ));

        if (card == null) {
            builder = new Card.Builder();

            accountEdit.setText("");
            numberEdit.setText("");
            commentEdit.setText("");
            enabledCheckBox.setSelected(true);
            typeComboBox.getSelectionModel().select(CardType.NONE);
            expritationDatePicker.setValue(LocalDate.now());
        } else {
            builder = new Card.Builder(card);

            accountEdit.setText(cache().getAccount(card.accountUuid()).map(Account::name).orElse(""));
            numberEdit.setText(card.number());
            commentEdit.setText(card.comment());
            enabledCheckBox.setSelected(card.enabled());
            typeComboBox.getSelectionModel().select(card.type());
            expritationDatePicker.setValue(card.expiration());
        }

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            var now = System.currentTimeMillis();

            builder.number(numberEdit.getText())
                    .type(typeComboBox.getValue())
                    .comment(commentEdit.getText())
                    .expiration(expritationDatePicker.getValue())
                    .enabled(enabledCheckBox.isSelected())
                    .modified(now);

            if (card == null) {
                builder.uuid(UUID.randomUUID())
                        .created(now);
            }

            return builder.build();
        });

        createDefaultButtons(UI, validation.invalidProperty());
        Platform.runLater(this::createValidationSupport);
    }

    private void createValidationSupport() {
        validation.registerValidator(accountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(accountEdit.getText(), accountSuggestionsAll, ACCOUNT_TO_STRING);
            builder.accountUuid(account.map(Account::uuid).orElse(null));
            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(numberEdit,
                (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isBlank()));
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value,
                                                              Collection<T> items,
                                                              StringConverter<T> converter) {
        return items.stream().filter(it -> converter.toString(it).equals(value)).findFirst();
    }

    private void setupAccountMenuItems() {
        var accounts = cache().getAccountsByType(CategoryType.BANKS_AND_CASH);
        accounts.stream()
                .sorted((a1, a2) -> a1.name().compareToIgnoreCase(a2.name()))
                .forEach(account -> {
                    accountSuggestionsAll.add(account);
                    if (account.enabled()) {
                        accountSuggestions.add(account);
                        accountMenuButton.getItems().add(
                                menuItem(account.name(), event -> onAccountSelected(account))
                        );
                    }
                });
    }

    private void onAccountSelected(Account account) {
        accountEdit.setText(account.name());
    }
}
