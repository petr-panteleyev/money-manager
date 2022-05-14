/*
 Copyright (c) 2017-2022, Petr Panteleyev

 This program is free software: you can redistribute it and/or modify it under the
 terms of the GNU General Public License as published by the Free Software
 Foundation, either version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful, but WITHOUT ANY
 WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with this
 program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.panteleyev.money.app;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.panteleyev.fx.BaseDialog;
import org.panteleyev.fx.Controller;
import org.panteleyev.money.app.icons.IconManager;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CardType;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.CategoryType;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Icon;
import org.panteleyev.money.persistence.DataCache;
import org.panteleyev.money.persistence.ReadOnlyNamedConverter;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.panteleyev.fx.FxFactory.newCheckBox;
import static org.panteleyev.fx.FxUtils.COLON;
import static org.panteleyev.fx.FxUtils.SKIP;
import static org.panteleyev.fx.FxUtils.fxString;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.combobox.ComboBoxBuilder.comboBox;
import static org.panteleyev.fx.grid.GridBuilder.gridCell;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.MainWindowController.UI;
import static org.panteleyev.money.app.icons.IconManager.EMPTY_ICON;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_ACCOUNT_NUMBER;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CARD_NUMBER;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CARD_TYPE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_CLOSING_DATE;
import static org.panteleyev.money.bundles.Internationalization.I18N_MISC_INITIAL_BALANCE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACCOUNT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ACTIVE;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CATEGORY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_COMMENT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CREDIT;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_CURRENCY;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_ENTITY_NAME;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_INTEREST;
import static org.panteleyev.money.bundles.Internationalization.I18N_WORD_TYPE;

class AccountDialog extends BaseDialog<Account> {
    private final ValidationSupport validation = new ValidationSupport();

    private final TextField nameEdit = new TextField();
    private final TextField initialEdit = new TextField();
    private final TextField creditEdit = new TextField();
    private final TextField commentEdit = new TextField();
    private final TextField accountNumberEdit = new TextField();
    private final ComboBox<CategoryType> typeComboBox = comboBox(CategoryType.values(),
            b -> b.withHandler(event -> onCategoryTypeSelected())
                    .withStringConverter(Bundles::translate));
    private final ComboBox<Category> categoryComboBox = new ComboBox<>();
    private final ComboBox<Currency> currencyComboBox = new ComboBox<>();
    private final CheckBox activeCheckBox = newCheckBox(UI, I18N_WORD_ACTIVE);
    private final TextField interestEdit = new TextField();
    private final DatePicker closingDatePicker = new DatePicker();
    private final ComboBox<Icon> iconComboBox = new ComboBox<>();
    private final ComboBox<CardType> cardTypeComboBox = comboBox(CardType.values(),
            b -> b.withDefaultString("-")
                    .withImageConverter(Images::getCardTypeIcon)
                    .withHandler(event -> onCardTypeSelected()));
    private final TextField cardNumberEdit = new TextField();

    private final Collection<Category> categories;

    AccountDialog(Controller owner, URL css, Category initialCategory) {
        this(owner, css, null, initialCategory, cache());
    }

    AccountDialog(Controller owner, URL css, Category initialCategory, DataCache cache) {
        this(owner, css, null, initialCategory, cache);
    }

    AccountDialog(Controller owner, URL css, Account account, Category initialCategory) {
        this(owner, css, account, initialCategory, cache());
    }

    AccountDialog(Controller owner, URL css, Account account, Category initialCategory, DataCache cache) {
        super(owner, css);
        setTitle(fxString(UI, I18N_WORD_ACCOUNT));

        getDialogPane().setContent(
                gridPane(
                        List.of(
                                gridRow(label(fxString(UI, I18N_WORD_ENTITY_NAME, COLON)), gridCell(nameEdit,
                                        2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_TYPE, COLON)), typeComboBox, iconComboBox),
                                gridRow(label(fxString(UI, I18N_WORD_CATEGORY, COLON)),
                                        gridCell(categoryComboBox, 2, 1)),
                                gridRow(label(fxString(UI, I18N_MISC_INITIAL_BALANCE, COLON)),
                                        gridCell(initialEdit, 2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_CREDIT, COLON)), gridCell(creditEdit, 2,
                                        1)),
                                gridRow(label(fxString(UI, I18N_MISC_ACCOUNT_NUMBER, COLON)),
                                        gridCell(accountNumberEdit, 2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_COMMENT, COLON)), gridCell(commentEdit,
                                        2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_CURRENCY, COLON)),
                                        gridCell(currencyComboBox, 2, 1)),
                                gridRow(label(fxString(UI, I18N_WORD_INTEREST, COLON)), gridCell(interestEdit
                                        , 2, 1)),
                                gridRow(label(fxString(UI, I18N_MISC_CLOSING_DATE, COLON)),
                                        gridCell(closingDatePicker, 2, 1)),
                                gridRow(label(fxString(UI, I18N_MISC_CARD_TYPE, COLON)),
                                        gridCell(cardTypeComboBox, 2, 1)),
                                gridRow(label(fxString(UI, I18N_MISC_CARD_NUMBER, COLON)),
                                        gridCell(cardNumberEdit, 2, 1)),
                                gridRow(SKIP, gridCell(activeCheckBox, 2, 1))
                        ), b -> b.withStyle(Styles.GRID_PANE)

                )
        );

        nameEdit.setPrefColumnCount(20);

        categories = cache.getCategories().sorted(Category.COMPARE_BY_NAME);

        categoryComboBox.setConverter(new ReadOnlyNamedConverter<>());
        currencyComboBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Currency currency) {
                return currency == null ? "" : currency.symbol();
            }
        });

        var currencyList = cache.getCurrencies();
        currencyComboBox.setItems(FXCollections.observableArrayList(currencyList));
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));

        IconManager.setupComboBox(iconComboBox);

        if (account == null) {
            nameEdit.setText("");
            initialEdit.setText("0.0");
            creditEdit.setText("0.0");
            activeCheckBox.setSelected(true);
            interestEdit.setText("0.0");
            closingDatePicker.setValue(null);
            iconComboBox.getSelectionModel().select(EMPTY_ICON);
            cardTypeComboBox.getSelectionModel().select(CardType.NONE);
            cardNumberEdit.setText("");

            if (initialCategory != null) {
                typeComboBox.getSelectionModel().select(initialCategory.type());
                onCategoryTypeSelected();
                categoryComboBox.getSelectionModel()
                        .select(cache.getCategory(initialCategory.uuid()).orElse(null));
            } else {
                typeComboBox.getSelectionModel().select(0);
                onCategoryTypeSelected();
            }

            currencyComboBox.getSelectionModel().select(cache.getDefaultCurrency().orElse(null));
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
            cardTypeComboBox.getSelectionModel().select(account.cardType());
            cardNumberEdit.setText(account.cardNumber());

            typeComboBox.getSelectionModel().select(account.type());
            categoryComboBox.getSelectionModel()
                    .select(cache.getCategory(account.categoryUuid()).orElse(null));
            currencyComboBox.getSelectionModel()
                    .select(cache.getCurrency(account.currencyUuid()).orElse(null));
        }
        onCardTypeSelected();

        setResultConverter((ButtonType b) -> {
            if (b != ButtonType.OK) {
                return null;
            }

            // TODO: reconsider using null currency value
            var selectedCurrency = currencyComboBox.getSelectionModel().getSelectedItem();

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
                    .currencyUuid(selectedCurrency != null ? selectedCurrency.uuid() : null)
                    .enabled(activeCheckBox.isSelected())
                    .interest(new BigDecimal(interestEdit.getText()))
                    .closingDate(closingDatePicker.getValue())
                    .iconUuid(uconUuid)
                    .cardType(cardTypeComboBox.getSelectionModel().getSelectedItem())
                    .cardNumber(cardNumberEdit.getText())
                    .modified(now);

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
    }

    private void onCardTypeSelected() {
        cardNumberEdit.setDisable(
                cardTypeComboBox.getSelectionModel().getSelectedItem() == CardType.NONE
        );
    }

    private void createValidationSupport() {
        validation.registerValidator(nameEdit,
                (Control control, String value) -> ValidationResult.fromErrorIf(control, null, value.isEmpty()));
        validation.registerValidator(categoryComboBox,
                (Control control, Category value) -> ValidationResult.fromErrorIf(control, null, value == null));
        validation.registerValidator(initialEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(creditEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.registerValidator(interestEdit, MainWindowController.BIG_DECIMAL_VALIDATOR);
        validation.initInitialDecoration();
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

    ComboBox<Currency> getCurrencyComboBox() {
        return currencyComboBox;
    }

    ComboBox<CardType> getCardTypeComboBox() {
        return cardTypeComboBox;
    }

    TextField getCardNumberEdit() {
        return cardNumberEdit;
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
}
