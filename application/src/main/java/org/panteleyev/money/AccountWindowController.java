package org.panteleyev.money;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakMapChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.cells.AccountBalanceCell;
import org.panteleyev.money.cells.AccountCardCell;
import org.panteleyev.money.cells.AccountCategoryCell;
import org.panteleyev.money.cells.AccountClosingDateCell;
import org.panteleyev.money.cells.AccountInterestCell;
import org.panteleyev.money.cells.AccountNameCell;
import org.panteleyev.money.filters.AccountNameFilterBox;
import org.panteleyev.money.filters.CategorySelectionBox;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.MoneyDAO;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.panteleyev.fx.MenuFactory.newCheckMenuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.Constants.ELLIPSIS;
import static org.panteleyev.money.MainWindowController.RB;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.Predicates.activeAccount;
import static org.panteleyev.money.persistence.DataCache.cache;
import static org.panteleyev.money.persistence.MoneyDAO.getDao;

final class AccountWindowController extends BaseController {
    // Filters
    private final CategorySelectionBox categorySelectionBox = new CategorySelectionBox();
    private final AccountNameFilterBox accountNameFilterBox = new AccountNameFilterBox();
    private final PredicateProperty<Account> showDeactivatedAccounts =
        new PredicateProperty<>(Options.getShowDeactivatedAccounts() ? a -> true : activeAccount(true));

    private final PredicateProperty<Account> filterProperty =
        PredicateProperty.and(List.of(
            categorySelectionBox.accountFilterProperty(),
            accountNameFilterBox.predicateProperty(),
            showDeactivatedAccounts
        ));

    // Items
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();
    private final SortedList<Account> sortedAccounts = new SortedList<>(accounts);
    private final FilteredList<Account> filteredAccounts = sortedAccounts.filtered(filterProperty.get());

    private final TableView<Account> tableView = new TableView<>(filteredAccounts);


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

    AccountWindowController() {
        setupTableColumns();
        createContextMenu();

        // Tool box
        var hBox = new HBox(5.0,
            accountNameFilterBox.getTextField(),
            categorySelectionBox
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        var centerBox = new BorderPane();
        centerBox.setTop(hBox);
        centerBox.setCenter(tableView);

        var self = new BorderPane();
        self.setTop(createMainMenu());
        self.setCenter(centerBox);

        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        sortedAccounts.setComparator(MoneyDAO.COMPARE_ACCOUNT_BY_CATEGORY
            .thenComparing(MoneyDAO.COMPARE_ACCOUNT_BY_NAME));

        categorySelectionBox.setupCategoryTypesBox();

        filteredAccounts.predicateProperty().bind(filterProperty);

        cache().categories().addListener(new WeakMapChangeListener<>(categoryListener));
        cache().accounts().addListener(new WeakMapChangeListener<>(accountListener));
        cache().transactions().addListener(new WeakMapChangeListener<>(transactionListener));

        initAccountList();

        setupWindow(self);
        Options.loadStageDimensions(getClass(), getStage());
    }

    @Override
    public String getTitle() {
        return RB.getString("Accounts");
    }

    private MenuBar createMainMenu() {
        var disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();

        var activateAccountMenuItem = newMenuItem(RB, "menu.edit.deactivate",
            event -> onActivateDeactivateAccount(),
            disableBinding);

        var editMenu = newMenu(RB, "menu.Edit",
            newMenuItem(RB, "Create", ELLIPSIS, event -> onNewAccount()),
            newMenuItem(RB, "menu.Edit.Edit", event -> onEditAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Delete", ELLIPSIS, event -> onDeleteAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.CopyName",
                new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN),
                event -> onCopyName(),
                disableBinding),
            newMenuItem(RB, "menu.edit.deactivate", event -> onActivateDeactivateAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.Edit.Search",
                new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                actionEvent -> accountNameFilterBox.getTextField().requestFocus()),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Transactions", ELLIPSIS, event -> onShowTransactions(), disableBinding)
        );

        editMenu.setOnShowing(event -> getSelectedAccount()
            .ifPresent(account -> activateAccountMenuItem.setText(RB.getString(
                account.getEnabled() ? "menu.edit.deactivate" : "menu.edit.activate")
            ))
        );

        return newMenuBar(
            newMenu(RB, "File",
                newMenuItem(RB, "Report", ELLIPSIS, event -> onReport()),
                new SeparatorMenuItem(),
                newMenuItem(RB, "Close", event -> onClose())),
            editMenu,
            newMenu(RB, "menu.view",
                newCheckMenuItem(RB, "check.showDeactivatedAccounts",
                    Options.getShowDeactivatedAccounts(),
                    new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN), x -> {
                        var selected = ((CheckMenuItem) x.getSource()).isSelected();
                        Options.setShowDeactivatedAccounts(selected);
                        showDeactivatedAccounts.set(selected ? a -> true : activeAccount(true));
                    }
                )
            ),
            createWindowMenu(RB),
            createHelpMenu(RB));
    }

    private void setupTableColumns() {
        var w = tableView.widthProperty().subtract(20);
        tableView.getColumns().setAll(List.of(
            newTableColumn(RB, "column.Name", x -> new AccountNameCell(), w.multiply(0.15)),
            newTableColumn(RB, "Category", x -> new AccountCategoryCell(), w.multiply(0.1)),
            newTableColumn(RB, "Currency", null, a -> cache().getCurrency(a.getCurrencyUuid().orElse(null))
                .map(Currency::getSymbol).orElse(""), w.multiply(0.05)),
            newTableColumn(RB, "Card", x -> new AccountCardCell(), w.multiply(0.1)),
            newTableColumn("%%", x -> new AccountInterestCell(), Account::getInterest, w.multiply(0.03)),
            newTableColumn(RB, "column.closing.date",
                x -> new AccountClosingDateCell(Options.getAccountClosingDayDelta()), w.multiply(0.05)),
            newTableColumn(RB, "Comment", null, Account::getComment, w.multiply(0.3)),
            newTableColumn(RB, "Balance", x -> new AccountBalanceCell(true, t -> true), w.multiply(0.1)),
            newTableColumn(RB, "Waiting", x -> new AccountBalanceCell(false, t -> !t.getChecked()), w.multiply(0.1))
        ));
    }

    private void createContextMenu() {
        var disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();

        var activateAccountMenuItem = newMenuItem(RB, "menu.edit.deactivate",
            event -> onActivateDeactivateAccount(),
            disableBinding);

        var contextMenu = new ContextMenu(
            newMenuItem(RB, "Create", ELLIPSIS, event -> onNewAccount()),
            newMenuItem(RB, "menu.Edit.Edit", event -> onEditAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Delete", ELLIPSIS, event -> onDeleteAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.CopyName",
                new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN),
                event -> onCopyName(),
                disableBinding),
            activateAccountMenuItem,
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.Edit.Search",
                new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN),
                actionEvent -> accountNameFilterBox.getTextField().requestFocus()),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Transactions", ELLIPSIS, event -> onShowTransactions(), disableBinding)
        );

        contextMenu.setOnShowing(event -> getSelectedAccount()
            .ifPresent(account -> activateAccountMenuItem.setText(RB.getString(
                account.getEnabled() ? "menu.edit.deactivate" : "menu.edit.activate")
            ))
        );

        tableView.setContextMenu(contextMenu);
    }

    private void initAccountList() {
        accounts.setAll(new ArrayList<>(cache().getAccounts()));
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void onNewAccount() {
        var initialCategory = getSelectedAccount()
            .flatMap(account -> cache().getCategory(account.getCategoryUuid()))
            .orElse(null);

        new AccountDialog(this, initialCategory).showAndWait().ifPresent(it -> getDao().insertAccount(it));
    }

    private void onEditAccount() {
        getSelectedAccount().flatMap(account -> new AccountDialog(this, account, null)
            .showAndWait())
            .ifPresent(it -> getDao().updateAccount(it));
    }

    private void onDeleteAccount() {
        getSelectedAccount().ifPresent(account -> {
            long count = cache().getTransactionCount(account);
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

    private void onShowTransactions() {
        getSelectedAccount().ifPresent(account ->
            getController(RequestWindowController.class).showTransactionsForAccount(account)
        );
    }

    private void onReport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(RB.getString("Report"));
        Options.getLastExportDir().ifPresent(fileChooser::setInitialDirectory);
        fileChooser.setInitialFileName(generateFileName("accounts"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        var selected = fileChooser.showSaveDialog(null);
        if (selected == null) {
            return;
        }

        try (var outputStream = new FileOutputStream(selected)) {
            var accounts = cache().getAccounts(filterProperty.get())
                .sorted(MoneyDAO.COMPARE_ACCOUNT_BY_CATEGORY.thenComparing(MoneyDAO.COMPARE_ACCOUNT_BY_NAME))
                .collect(Collectors.toList());
            Reports.reportAccounts(accounts, outputStream);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
