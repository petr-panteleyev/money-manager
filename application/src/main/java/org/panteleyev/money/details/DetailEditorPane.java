/*
 * Copyright (c) 2019, 2020, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.details;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.panteleyev.money.BaseCompletionProvider;
import org.panteleyev.money.Options;
import org.panteleyev.money.RecordEditorCallback;
import org.panteleyev.money.Styles;
import org.panteleyev.money.ToStringConverter;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Named;
import org.panteleyev.money.model.TransactionDetail;
import org.panteleyev.money.persistence.DataCache;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import static org.panteleyev.fx.FxFactory.newButton;
import static org.panteleyev.fx.FxFactory.newLabel;
import static org.panteleyev.fx.FxFactory.newMenuItem;

final class DetailEditorPane extends BorderPane {
    private static final ToStringConverter<Account> ACCOUNT_TO_STRING = new ToStringConverter<>() {
        public String toString(Account obj) {
            return obj.getName();
        }
    };

    private static class CompletionProvider<T extends Named> extends BaseCompletionProvider<T> {
        CompletionProvider(Set<T> set) {
            super(set, Options::getAutoCompleteLength);
        }

        public String getElementString(T element) {
            return element.getName();
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

    private static final ResourceBundle RB = ResourceBundle.getBundle("org.panteleyev.money.res.TransactionEditorPane");

    private final DataCache cache;

    private final TextField creditedAccountEdit = new TextField();
    private final TextField sumEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final Label creditedCategoryLabel = new Label();

    private final MenuButton creditedMenuButton = new MenuButton();

    private TransactionDetail transactionDetail = null;

    private final TreeSet<Account> creditedSuggestions = new TreeSet<>();
    private final TreeSet<String> commentSuggestions = new TreeSet<>();

    private final ValidationSupport validation = new ValidationSupport();

    private final SimpleBooleanProperty newTransactionProperty = new SimpleBooleanProperty(true);

    private final Validator<String> DECIMAL_VALIDATOR = (Control control, String value) -> {
        var invalid = false;
        try {
            new BigDecimal(value);
        } catch (NumberFormatException ex) {
            invalid = true;
        }

        return ValidationResult.fromErrorIf(control, null, invalid && !control.isDisabled());
    };

    DetailEditorPane(RecordEditorCallback<TransactionDetail> parent, DataCache cache) {
        this.cache = cache;

        commentEdit.setPrefColumnCount(30);
        sumEdit.setPrefColumnCount(8);

        var creditedBox = new VBox(Styles.SMALL_SPACING, newLabel(RB, "creditedAccountLabel"),
            new HBox(creditedAccountEdit, creditedMenuButton),
            creditedCategoryLabel);
        HBox.setHgrow(creditedAccountEdit, Priority.ALWAYS);

        var hBox1 = new HBox(Styles.BIG_SPACING, sumEdit);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        var sumBox = new VBox(Styles.SMALL_SPACING, newLabel(RB, "sumLabel"), hBox1);

        var commentBox = new VBox(Styles.SMALL_SPACING, newLabel(RB, "commentLabel"), commentEdit);

        var filler = new Region();

        var clearButton = newButton(RB, "clearButton", x -> clear());
        clearButton.setCancelButton(true);

        var addButton = newButton(RB, "addButton", x -> buildTransactionDetail()
            .ifPresent(t -> {
                parent.addRecord(t);
                clear();
            }));

        var updateButton = newButton(RB, "updateButton", x -> buildTransactionDetail()
            .ifPresent(t -> {
                parent.updateRecord(t);
                clear();
            })
        );

        var deleteButton = newButton(RB, "deleteButton", x -> {
            if (transactionDetail != null) {
                parent.deleteRecord(transactionDetail);
                clear();
            }
        });

        var row3 = new HBox(Styles.BIG_SPACING,
            clearButton, deleteButton, updateButton, addButton);
        row3.setAlignment(Pos.CENTER_LEFT);

        setCenter(new VBox(Styles.BIG_SPACING,
            new HBox(Styles.BIG_SPACING,
                creditedBox, commentBox, sumBox),
            new HBox(Styles.BIG_SPACING),
            row3));

        HBox.setHgrow(creditedBox, Priority.ALWAYS);
        HBox.setHgrow(commentBox, Priority.ALWAYS);
        HBox.setHgrow(filler, Priority.ALWAYS);

        creditedMenuButton.setFocusTraversable(false);

        creditedCategoryLabel.getStyleClass().add(Styles.SUB_LABEL);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        deleteButton.disableProperty().bind(newTransactionProperty);
        updateButton.disableProperty().bind(validation.invalidProperty().or(newTransactionProperty));
        addButton.disableProperty().bind(validation.invalidProperty());

        TextFields.bindAutoCompletion(creditedAccountEdit,
            new CompletionProvider<>(creditedSuggestions), ACCOUNT_TO_STRING);
        TextFields.bindAutoCompletion(commentEdit, new StringCompletionProvider(commentSuggestions));

        setupAccountMenus();

        Platform.runLater(this::createValidationSupport);
    }

    private void setupBanksAndCashMenuItems(Set<Account> creditedSuggestions) {
        var banksAndCash = cache.getAccountsByType(CategoryType.BANKS_AND_CASH).stream()
            .filter(Account::getEnabled)
            .collect(Collectors.toList());

        banksAndCash.stream()
            .sorted((a1, a2) -> a1.getName().compareToIgnoreCase(a2.getName()))
            .forEach(acc -> {
                creditedMenuButton.getItems().add(
                    newMenuItem('[' + acc.getName() + ']',
                        event -> onCreditedAccountSelected(acc)));
                creditedSuggestions.add(acc);
            });

        if (!banksAndCash.isEmpty()) {
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    private void setupDebtMenuItems(Set<Account> creditedSuggestions) {
        setAccountMenuItemsByCategory(CategoryType.DEBTS, "!", creditedSuggestions);
    }

    private void setupAssetsMenuItems(Set<Account> creditedSuggestions) {
        setAccountMenuItemsByCategory(CategoryType.ASSETS, ".", creditedSuggestions);
    }

    private void setAccountMenuItemsByCategory(CategoryType categoryType, String prefix,
                                               Set<Account> creditedSuggestions)
    {
        var categories = cache.getCategoriesByType(categoryType);

        categories.stream()
            .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
            .forEach(x -> {
                List<Account> accounts = cache.getAccountsByCategory(x.getUuid());

                if (!accounts.isEmpty()) {
                    creditedMenuButton.getItems().add(new MenuItem(x.getName()));

                    accounts.stream()
                        .filter(Account::getEnabled)
                        .forEach(acc -> {
                            creditedMenuButton.getItems().add(
                                newMenuItem("  " + prefix + ' ' + acc.getName(),
                                    event -> onCreditedAccountSelected(acc)));
                            creditedSuggestions.add(acc);
                        });
                }
            });


        if (!categories.isEmpty()) {
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }
    }

    private void clear() {
        transactionDetail = null;

        newTransactionProperty.set(true);

        creditedAccountEdit.setText("");
        commentEdit.setText("");
        sumEdit.setText("");
    }

    void setTransactionDetail(TransactionDetail tr) {
        transactionDetail = tr;

        if (tr == null) {
            clear();
        } else {
            newTransactionProperty.set(false);

            // Accounts
            Optional<Account> accCredited = cache.getAccount(tr.getAccountCreditedUuid());
            creditedAccountEdit.setText(accCredited.map(Account::getName).orElse(""));

            // Other fields
            commentEdit.setText(tr.getComment());

            // Sum
            sumEdit.setText(tr.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }

    private void onCreditedAccountSelected(Account acc) {
        creditedAccountEdit.setText(acc.getName());
    }

    private Optional<TransactionDetail> buildTransactionDetail() {
        var builder = new TransactionDetail.Builder(transactionDetail == null ? null : transactionDetail.getUuid());

        var creditedAccount = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
        if (creditedAccount.isPresent()) {
            builder.accountCreditedUuid(creditedAccount.get().getUuid());
        } else {
            return Optional.empty();
        }

        builder.comment(commentEdit.getText());

        try {
            builder.amount(new BigDecimal(sumEdit.getText()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }

        return Optional.of(builder.build());
    }

    private <T extends Named> Optional<T> checkTextFieldValue(String value,
                                                              Collection<T> items,
                                                              StringConverter<T> converter)
    {
        return items.stream().filter(it -> converter.toString(it).equals(value)).findFirst();
    }

    private <T extends Named> Optional<T> checkTextFieldValue(TextField field,
                                                              Collection<T> items,
                                                              StringConverter<T> converter)
    {
        return checkTextFieldValue(field.getText(), items, converter);
    }

    private void createValidationSupport() {
        validation.registerValidator(creditedAccountEdit, (Control control, String value) -> {
            var account = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
            updateCategoryLabel(creditedCategoryLabel, account.orElse(null));

            return ValidationResult.fromErrorIf(control, null, account.isEmpty());
        });

        validation.registerValidator(sumEdit, DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
    }

    private void setupAccountMenus() {
        creditedMenuButton.getItems().clear();
        creditedSuggestions.clear();

        // Bank and cash accounts first
        setupBanksAndCashMenuItems(creditedSuggestions);

        // Expenses to creditable accounts
        List<Category> expenseCategories = cache.getCategoriesByType(CategoryType.EXPENSES);
        expenseCategories.stream()
            .sorted((c1, c2) -> c1.getName().compareToIgnoreCase(c2.getName()))
            .forEach(x -> {
                var accounts = cache.getAccountsByCategory(x.getUuid());

                if (!accounts.isEmpty()) {
                    creditedMenuButton.getItems().add(new MenuItem(x.getName()));

                    accounts.forEach(acc -> {
                        creditedSuggestions.add(acc);
                        creditedMenuButton.getItems().add(
                            newMenuItem("  - " + acc.getName(), event -> onCreditedAccountSelected(acc)));
                    });
                }
            });

        if (!expenseCategories.isEmpty()) {
            creditedMenuButton.getItems().add(new SeparatorMenuItem());
        }

        setupDebtMenuItems(creditedSuggestions);
        setupAssetsMenuItems(creditedSuggestions);
    }

    private void updateCategoryLabel(Label label, Account account) {
        if (account != null) {
            var catName = cache.getCategory(account.getCategoryUuid()).map(Category::getName).orElse("");
            label.setText(account.getType().getTypeName() + " | " + catName);
        } else {
            label.setText("");
        }
    }
}
