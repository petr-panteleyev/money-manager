/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */
package org.panteleyev.money.app.details;

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
import org.panteleyev.money.app.BaseCompletionProvider;
import org.panteleyev.money.app.RecordEditorCallback;
import org.panteleyev.money.app.Styles;
import org.panteleyev.money.app.ToStringConverter;
import org.panteleyev.money.app.options.Options;
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
import java.util.Set;
import java.util.TreeSet;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.ButtonFactory.button;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.MainWindowController.RB;

final class DetailEditorPane extends BorderPane {
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

    private static class StringCompletionProvider extends BaseCompletionProvider<String> {
        StringCompletionProvider(Set<String> set) {
            super(set, Options::getAutoCompleteLength);
        }

        public String getElementString(String element) {
            return element;
        }
    }

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

        var creditedBox = new VBox(Styles.SMALL_SPACING, label(fxString(RB, "Credited_Account", COLON)),
            new HBox(creditedAccountEdit, creditedMenuButton),
            creditedCategoryLabel);
        HBox.setHgrow(creditedAccountEdit, Priority.ALWAYS);

        var hBox1 = hBox(Styles.BIG_SPACING, sumEdit);
        hBox1.setAlignment(Pos.CENTER_LEFT);
        var sumBox = vBox(Styles.SMALL_SPACING, label(fxString(RB, "Sum", COLON)), hBox1);

        var commentBox = vBox(Styles.SMALL_SPACING, label(fxString(RB, "Comment", COLON)), commentEdit);

        var filler = new Region();

        var clearButton = button(fxString(RB, "Clear"), x -> clear());
        clearButton.setCancelButton(true);

        var addButton = button(fxString(RB, "Add"), x -> buildTransactionDetail()
            .ifPresent(t -> {
                parent.addRecord(t);
                clear();
            }));

        var updateButton = button(fxString(RB, "Update"), x -> buildTransactionDetail()
            .ifPresent(t -> {
                parent.updateRecord(t);
                clear();
            })
        );

        var deleteButton = button(fxString(RB, "Delete"), x -> {
            if (transactionDetail != null) {
                parent.deleteRecord(transactionDetail);
                clear();
            }
        });

        var row3 = hBox(Styles.BIG_SPACING,
            clearButton, deleteButton, updateButton, addButton);
        row3.setAlignment(Pos.CENTER_LEFT);

        setCenter(new VBox(Styles.BIG_SPACING,
            hBox(Styles.BIG_SPACING,
                creditedBox, commentBox, sumBox),
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
            .filter(Account::enabled)
            .toList();

        banksAndCash.stream()
            .sorted((a1, a2) -> a1.name().compareToIgnoreCase(a2.name()))
            .forEach(acc -> {
                creditedMenuButton.getItems().add(
                    menuItem('[' + acc.name() + ']',
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
            .sorted((c1, c2) -> c1.name().compareToIgnoreCase(c2.name()))
            .forEach(x -> {
                List<Account> accounts = cache.getAccountsByCategory(x.uuid());

                if (!accounts.isEmpty()) {
                    creditedMenuButton.getItems().add(new MenuItem(x.name()));

                    accounts.stream()
                        .filter(Account::enabled)
                        .forEach(acc -> {
                            creditedMenuButton.getItems().add(
                                menuItem("  " + prefix + ' ' + acc.name(),
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
            var accCredited = cache.getAccount(tr.accountCreditedUuid());
            creditedAccountEdit.setText(accCredited.map(Account::name).orElse(""));

            // Other fields
            commentEdit.setText(tr.comment());

            // Sum
            sumEdit.setText(tr.amount().setScale(2, RoundingMode.HALF_UP).toString());
        }
    }

    private void onCreditedAccountSelected(Account acc) {
        creditedAccountEdit.setText(acc.name());
    }

    private Optional<TransactionDetail> buildTransactionDetail() {
        var builder = new TransactionDetail.Builder(transactionDetail == null ? null : transactionDetail.uuid());

        var creditedAccount = checkTextFieldValue(creditedAccountEdit, creditedSuggestions, ACCOUNT_TO_STRING);
        if (creditedAccount.isPresent()) {
            builder.accountCreditedUuid(creditedAccount.get().uuid());
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
        var expenseCategories = cache.getCategoriesByType(CategoryType.EXPENSES);
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

        setupDebtMenuItems(creditedSuggestions);
        setupAssetsMenuItems(creditedSuggestions);
    }

    private void updateCategoryLabel(Label label, Account account) {
        if (account != null) {
            var catName = cache.getCategory(account.categoryUuid()).map(Category::name).orElse("");
            label.setText(account.type() + " | " + catName);
        } else {
            label.setText("");
        }
    }
}
