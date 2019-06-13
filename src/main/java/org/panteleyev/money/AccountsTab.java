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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.panteleyev.money.cells.AccountBalanceCell;
import org.panteleyev.money.cells.AccountCategoryCell;
import org.panteleyev.money.cells.AccountInterestCell;
import org.panteleyev.money.cells.AccountNameCell;
import org.panteleyev.money.cells.LocalDateCell;
import org.panteleyev.money.filters.AccountActiveFilter;
import org.panteleyev.money.filters.AccountCategoryFilter;
import org.panteleyev.money.filters.AccountTypeFilter;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;
import org.panteleyev.money.persistence.model.Account;
import org.panteleyev.money.persistence.model.Category;
import org.panteleyev.money.persistence.model.CategoryType;
import org.panteleyev.money.persistence.model.Currency;
import org.panteleyev.money.persistence.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import static org.panteleyev.money.FXFactory.newMenuItem;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

class AccountsTab extends BorderPane {
    private static class TypeListItem {
        private final String text;
        private final EnumSet<CategoryType> types;
        private final boolean allTypes;

        TypeListItem(String text) {
            this.text = text;
            this.types = EnumSet.allOf(CategoryType.class);
            this.allTypes = true;
        }

        TypeListItem(String text, CategoryType type, CategoryType... types) {
            this.text = text;
            this.types = EnumSet.of(type, types);
            this.allTypes = false;
        }

        String getText() {
            return text;
        }

        EnumSet<CategoryType> getTypes() {
            return types;
        }

        boolean isAllTypes() {
            return allTypes;
        }
    }

    // Items
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final SortedList<Account> sortedAccounts = new SortedList<>(accounts);
    private final FilteredList<Account> filteredAccounts =
        sortedAccounts.filtered(new AccountActiveFilter(Options.getShowDeactivatedAccounts()));

    private final TableView<Account> tableView = new TableView<>(filteredAccounts);

    // Filters
    private Predicate<Account> accountFilter;
    private final ChoiceBox<Object> accountFilterBox = new ChoiceBox<>();
    private final ChoiceBox<Object> categoryChoiceBox = new ChoiceBox<>();
    private final CheckBox showDeactivatedAccountsCheckBox =
        new CheckBox(RB.getString("check.showDeactivatedAccounts"));
    private final TextField searchField = FXFactory.newSearchField(x -> updateFilters());

    // Listeners
    private Consumer<Account> accountTransactionsConsumer = x -> { };

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Category> categoryListener =
        (MapChangeListener.Change<? extends UUID, ? extends Category> change) ->
            Platform.runLater(() -> {
                tableView.getColumns().get(0).setVisible(false);
                tableView.getColumns().get(0).setVisible(true);
            });

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Account> accountListener =
        (MapChangeListener.Change<? extends UUID, ? extends Account> change) ->
            Platform.runLater(() -> handleAccountMapChange(change));

    @SuppressWarnings("FieldCanBeLocal")
    private final MapChangeListener<UUID, Transaction> transactionListener =
        change -> Platform.runLater(tableView::refresh);

    AccountsTab() {
        setupTableColumns();

        // Context menu
        var addAccountMenuItem = newMenuItem(RB, "menu.Edit.newAccount", event -> onNewAccount());
        var editAccountMenuItem = newMenuItem(RB, "menu.Edit.Edit", event -> onEditAccount());
        editAccountMenuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        var deleteAccountMenuItem = newMenuItem(RB, "menu.Edit.Delete", event -> onDeleteAccount());
        deleteAccountMenuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        var activateAccountMenuItem = newMenuItem(RB, "menu.edit.deactivate", event -> onActivateDeactivateAccount());
        activateAccountMenuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        var copyNameMenuItem = newMenuItem(RB, "menu.CopyName", event -> onCopyName());
        copyNameMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copyNameMenuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        var showTransactionsMenuItem = newMenuItem(RB, "menu.show.transactions", event -> onShowTransactions());
        showTransactionsMenuItem.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        var searchMenuItem = newMenuItem(RB, "menu.Edit.Search",
            new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
            actionEvent -> searchField.requestFocus());


        var contextMenu = new ContextMenu(
            addAccountMenuItem,
            editAccountMenuItem,
            new SeparatorMenuItem(),
            deleteAccountMenuItem,
            new SeparatorMenuItem(),
            copyNameMenuItem,
            activateAccountMenuItem,
            new SeparatorMenuItem(),
            searchMenuItem,
            new SeparatorMenuItem(),
            showTransactionsMenuItem
        );

        contextMenu.setOnShowing(event -> getSelectedAccount()
            .ifPresent(account -> activateAccountMenuItem.setText(RB.getString(
                account.getEnabled() ? "menu.edit.deactivate" : "menu.edit.activate")
            ))
        );

        tableView.setContextMenu(contextMenu);

        // Tool box
        var hBox = new HBox(5.0,
            searchField,
            accountFilterBox,
            categoryChoiceBox,
            showDeactivatedAccountsCheckBox
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        setTop(hBox);
        setCenter(tableView);

        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        sortedAccounts.setComparator(Account.COMPARE_BY_CATEGORY.thenComparing(Account.COMPARE_BY_NAME));

        initAccountFilterBox();

        showDeactivatedAccountsCheckBox.setSelected(Options.getShowDeactivatedAccounts());
        showDeactivatedAccountsCheckBox.setOnAction(event -> onShowDeactivatedAccounts());

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Category) {
                    return ((Category) object).getName();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });
        categoryChoiceBox.setItems(FXCollections.observableArrayList(RB.getString("account.Window.AllCategories")));
        categoryChoiceBox.getSelectionModel().select(0);
        categoryChoiceBox.valueProperty().addListener((x, y, z) -> updateFilters());

        getDao().categories().addListener(categoryListener);
        getDao().accounts().addListener(accountListener);
        getDao().transactions().addListener(transactionListener);

        getDao().preloadingProperty().addListener((x, y, newValue) -> {
            if (!newValue) {
                Platform.runLater(this::initAccountFilterBox);
                Platform.runLater(this::initAccountList);
            }
        });
    }

    Predicate<Account> getAccountFilter() {
        return accountFilter;
    }

    private void setupTableColumns() {
        // Table
        var nameColumn = new TableColumn<Account, Account>(RB.getString("column.Name"));
        var categoryColumn = new TableColumn<Account, Account>(RB.getString("column.Category"));
        var commentColumn = new TableColumn<Account, String>(RB.getString("column.Comment"));
        var currencyColumn = new TableColumn<Account, String>(RB.getString("column.Currency"));
        var approvedColumn = new TableColumn<Account, Account>(RB.getString("column.Approved"));
        var balanceColumn = new TableColumn<Account, Account>(RB.getString("column.Balance"));
        var waitingColumn = new TableColumn<Account, Account>(RB.getString("column.Waiting"));
        var interestColumn = new TableColumn<Account, BigDecimal>("%%");
        var closingDateColumn = new TableColumn<Account, LocalDate>(RB.getString("column.closing.date"));

        //noinspection unchecked
        tableView.getColumns().setAll(
            nameColumn,
            categoryColumn,
            currencyColumn,
            interestColumn,
            closingDateColumn,
            commentColumn,
            balanceColumn,
            approvedColumn,
            waitingColumn
        );

        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Account> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        nameColumn.setCellFactory((x) -> new AccountNameCell());
        categoryColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Account> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        categoryColumn.setCellFactory(x -> new AccountCategoryCell());
        commentColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getComment()));
        currencyColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
            new ReadOnlyObjectWrapper<>(getDao().getCurrency(p.getValue().getCurrencyUuid().orElse(null))
                .map(Currency::getSymbol).orElse("")));
        approvedColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Account> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        approvedColumn.setCellFactory((x) -> new AccountBalanceCell(true, Transaction::getChecked));
        balanceColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Account> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        balanceColumn.setCellFactory((x) -> new AccountBalanceCell(true, t -> true));
        waitingColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Account> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue()));
        waitingColumn.setCellFactory((x) -> new AccountBalanceCell(true, t -> !t.getChecked()));
        interestColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, BigDecimal> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getInterest()));
        interestColumn.setCellFactory((x) -> new AccountInterestCell());
        closingDateColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, LocalDate> p) ->
            new ReadOnlyObjectWrapper<>(p.getValue().getClosingDate().orElse(null)));
        closingDateColumn.setCellFactory((x) -> new LocalDateCell<>());

        nameColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.15));
        categoryColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1));
        commentColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.32));
        currencyColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.05));
        approvedColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1));
        balanceColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1));
        waitingColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.1));
        interestColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.03));
        closingDateColumn.prefWidthProperty().bind(tableView.widthProperty().subtract(20).multiply(0.05));
    }

    private void initAccountFilterBox() {
        accountFilterBox.getItems().setAll(
            new TypeListItem(RB.getString("text.AccountsCashCards"), CategoryType.BANKS_AND_CASH, CategoryType.DEBTS),
            new TypeListItem(RB.getString("account.Tree.IncomesExpenses"), CategoryType.INCOMES, CategoryType.EXPENSES),
            new Separator(),
            new TypeListItem(RB.getString("text.All.Accounts")),
            new Separator()
        );

        for (var t : CategoryType.values()) {
            accountFilterBox.getItems().add(new TypeListItem(t.getTypeName(), t));
        }

        accountFilterBox.getSelectionModel().selectedItemProperty()
            .addListener((x, y, newValue) -> onTypeChanged(newValue));

        accountFilterBox.setConverter(new ReadOnlyStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof TypeListItem) {
                    return ((TypeListItem) object).getText();
                } else {
                    return object != null ? object.toString() : "-";
                }
            }
        });

        accountFilterBox.getSelectionModel().select(0);
    }

    private void initAccountList() {
        updateFilters();
        accounts.setAll(new ArrayList<>(getDao().getAccounts()));
    }

    private void updateFilters() {
        Predicate<Account> filter = new AccountActiveFilter(Options.getShowDeactivatedAccounts());

        var catObject = categoryChoiceBox.getSelectionModel().getSelectedItem();
        if (catObject instanceof Category) {
            filter = filter.and(new AccountCategoryFilter((Category) catObject));
        } else {
            var typeListItem = accountFilterBox.getSelectionModel().getSelectedItem();
            if (typeListItem instanceof TypeListItem) {
                filter = filter.and(new AccountTypeFilter(((TypeListItem) typeListItem).getTypes()));
            }
        }

        var searchText = searchField.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filter = filter.and(account -> account.getName().toLowerCase().contains(searchText));
        }

        accountFilter = filter;
        filteredAccounts.setPredicate(accountFilter);
    }

    private void onShowDeactivatedAccounts() {
        Options.setShowDeactivatedAccounts(showDeactivatedAccountsCheckBox.isSelected());
        updateFilters();
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void onNewAccount() {
        Category initialCategory = getSelectedAccount().map(account -> getDao().getCategory(account.getCategoryUuid())
            .orElse(null)).orElse(null);

        new AccountDialog(initialCategory).showAndWait().ifPresent(it -> getDao().insertAccount(it));
    }

    private void onEditAccount() {
        getSelectedAccount().ifPresent(account ->
            new AccountDialog(account, null).showAndWait()
                .ifPresent(it -> getDao().updateAccount(it)));
    }

    private void onDeleteAccount() {
        getSelectedAccount().ifPresent(account -> {
            long count = getDao().getTransactionCount(account);
            if (count != 0L) {
                new Alert(Alert.AlertType.ERROR,
                    "Unable to delete account\nwith " + count + " associated transactions",
                    ButtonType.CLOSE).showAndWait();
            } else {
                new Alert(Alert.AlertType.CONFIRMATION, RB.getString("text.AreYouSure"), ButtonType.OK,
                    ButtonType.CANCEL)
                    .showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(b -> getDao().deleteAccount(account));
            }
        });
    }

    private void onActivateDeactivateAccount() {
        getSelectedAccount().ifPresent(account -> {
            boolean enabled = account.getEnabled();
            getDao().updateAccount(account.enable(!enabled));
        });
    }

    private void onCopyName() {
        getSelectedAccount().map(Account::getName).ifPresent(name -> {
            Clipboard cb = Clipboard.getSystemClipboard();
            ClipboardContent ct = new ClipboardContent();
            ct.putString(name);
            cb.setContent(ct);
        });
    }

    // Must be executed in UI thread
    private void handleAccountMapChange(MapChangeListener.Change<? extends UUID, ? extends Account> change) {
        var removedAccount = change.getValueRemoved();
        var addedAccount = change.getValueAdded();
        if (removedAccount == null && addedAccount == null) {
            return;
        }

        if (removedAccount == null) {
            if (Options.getShowDeactivatedAccounts() || addedAccount.getEnabled()) {
                accounts.add(addedAccount);
            }
        } else {
            var index = accounts.indexOf(removedAccount);
            if (index == -1) {
                return;
            }

            if (addedAccount == null) {
                // Remove account
                accounts.remove(index);
            } else {
                // Update account
                accounts.set(index, addedAccount);
            }
        }
    }

    private void onTypeChanged(Object object) {
        if (!(object instanceof TypeListItem)) {
            return;
        }
        var typeListItem = (TypeListItem) object;

        ObservableList<Object> items;

        if (typeListItem.isAllTypes()) {
            items = FXCollections.observableArrayList(RB.getString("account.Window.AllCategories"));
        } else {
            items = FXCollections.observableArrayList(getDao().getCategoriesByType(typeListItem.getTypes()));

            if (!items.isEmpty()) {
                items.add(0, new Separator());
            }
            items.add(0, RB.getString("account.Window.AllCategories"));
        }

        categoryChoiceBox.setItems(items);
        categoryChoiceBox.getSelectionModel().select(0);
    }

    private void onShowTransactions() {
        getSelectedAccount().ifPresent(account -> accountTransactionsConsumer.accept(account));
    }

    void setAccountTransactionsConsumer(Consumer<Account> accountTransactionsConsumer) {
        this.accountTransactionsConsumer = accountTransactionsConsumer;
    }
}
