package org.panteleyev.money.app;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.cells.AccountBalanceCell;
import org.panteleyev.money.app.cells.AccountCardCell;
import org.panteleyev.money.app.cells.AccountCategoryCell;
import org.panteleyev.money.app.cells.AccountClosingDateCell;
import org.panteleyev.money.app.cells.AccountInterestCell;
import org.panteleyev.money.app.cells.AccountNameCell;
import org.panteleyev.money.app.filters.AccountNameFilterBox;
import org.panteleyev.money.app.filters.CategorySelectionBox;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.Transaction;
import org.panteleyev.money.persistence.MoneyDAO;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.panteleyev.fx.MenuFactory.newCheckMenuItem;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.MenuFactory.newMenuBar;
import static org.panteleyev.fx.MenuFactory.newMenuItem;
import static org.panteleyev.fx.TableFactory.newTableColumn;
import static org.panteleyev.money.app.Constants.ELLIPSIS;
import static org.panteleyev.money.app.Constants.SHORTCUT_C;
import static org.panteleyev.money.app.Constants.SHORTCUT_DELETE;
import static org.panteleyev.money.app.Constants.SHORTCUT_E;
import static org.panteleyev.money.app.Constants.SHORTCUT_F;
import static org.panteleyev.money.app.Constants.SHORTCUT_H;
import static org.panteleyev.money.app.Constants.SHORTCUT_N;
import static org.panteleyev.money.app.Constants.SHORTCUT_T;
import static org.panteleyev.money.app.MainWindowController.RB;
import static org.panteleyev.money.MoneyApplication.generateFileName;
import static org.panteleyev.money.app.Predicates.activeAccount;
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
    private final FilteredList<Account> filteredAccounts = cache().getAccounts().filtered(filterProperty.get());
    private final TableView<Account> tableView = new TableView<>(
        filteredAccounts.sorted(
            MoneyDAO.COMPARE_ACCOUNT_BY_CATEGORY.thenComparing(MoneyDAO.COMPARE_ACCOUNT_BY_NAME)
        )
    );

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Category> categoryListener = c -> Platform.runLater(() -> {
        tableView.getColumns().get(0).setVisible(false);
        tableView.getColumns().get(0).setVisible(true);
    });

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Transaction> transactionListener = c -> Platform.runLater(tableView::refresh);

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

        categorySelectionBox.setupCategoryTypesBox();

        filteredAccounts.predicateProperty().bind(filterProperty);

        cache().getCategories().addListener(new WeakListChangeListener<>(categoryListener));
        cache().getTransactions().addListener(new WeakListChangeListener<>(transactionListener));

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
            newMenuItem(RB, "Create", ELLIPSIS, SHORTCUT_N,
                event -> onNewAccount()),
            newMenuItem(RB, "menu.Edit.Edit", ELLIPSIS, SHORTCUT_E,
                event -> onEditAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Delete", ELLIPSIS, SHORTCUT_DELETE,
                event -> onDeleteAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.CopyName", SHORTCUT_C,
                event -> onCopyName(), disableBinding),
            newMenuItem(RB, "menu.edit.deactivate",
                event -> onActivateDeactivateAccount(), disableBinding),
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.Edit.Search", SHORTCUT_F,
                event -> accountNameFilterBox.getTextField().requestFocus()),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Transactions", ELLIPSIS, SHORTCUT_T,
                event -> onShowTransactions(), disableBinding)
        );

        editMenu.setOnShowing(event -> getSelectedAccount()
            .ifPresent(account -> activateAccountMenuItem.setText(RB.getString(
                account.enabled() ? "menu.edit.deactivate" : "menu.edit.activate")
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
                    Options.getShowDeactivatedAccounts(), SHORTCUT_H,
                    event -> {
                        var selected = ((CheckMenuItem) event.getSource()).isSelected();
                        Options.setShowDeactivatedAccounts(selected);
                        showDeactivatedAccounts.set(selected ? a -> true : activeAccount(true));
                    }
                )
            ),
            createWindowMenu(),
            createHelpMenu());
    }

    private void setupTableColumns() {
        var w = tableView.widthProperty().subtract(20);
        tableView.getColumns().setAll(List.of(
            newTableColumn(RB, "column.Name", x -> new AccountNameCell(), w.multiply(0.15)),
            newTableColumn(RB, "Category", x -> new AccountCategoryCell(), w.multiply(0.1)),
            newTableColumn(RB, "Currency", null, a -> cache().getCurrency(a.currencyUuid())
                .map(Currency::symbol).orElse(""), w.multiply(0.05)),
            newTableColumn(RB, "Card", x -> new AccountCardCell(), w.multiply(0.1)),
            newTableColumn("%%", x -> new AccountInterestCell(), Account::interest, w.multiply(0.03)),
            newTableColumn(RB, "column.closing.date",
                x -> new AccountClosingDateCell(Options.getAccountClosingDayDelta()), w.multiply(0.05)),
            newTableColumn(RB, "Comment", null, Account::comment, w.multiply(0.3)),
            newTableColumn(RB, "Balance", x -> new AccountBalanceCell(true, t -> true), w.multiply(0.1)),
            newTableColumn(RB, "Waiting", x -> new AccountBalanceCell(false, t -> !t.checked()), w.multiply(0.1))
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
            newMenuItem(RB, "menu.CopyName", event -> onCopyName(), disableBinding),
            activateAccountMenuItem,
            new SeparatorMenuItem(),
            newMenuItem(RB, "menu.Edit.Search", actionEvent -> accountNameFilterBox.getTextField().requestFocus()),
            new SeparatorMenuItem(),
            newMenuItem(RB, "Transactions", ELLIPSIS, event -> onShowTransactions(), disableBinding)
        );

        contextMenu.setOnShowing(event -> getSelectedAccount()
            .ifPresent(account -> activateAccountMenuItem.setText(RB.getString(
                account.enabled() ? "menu.edit.deactivate" : "menu.edit.activate")
            ))
        );

        tableView.setContextMenu(contextMenu);
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void onNewAccount() {
        var initialCategory = getSelectedAccount()
            .flatMap(account -> cache().getCategory(account.categoryUuid()))
            .orElse(null);

        new AccountDialog(this, initialCategory)
            .showAndWait()
            .ifPresent(account -> {
                getDao().insertAccount(account);
                tableView.scrollTo(account);
                tableView.getSelectionModel().select(account);
            });
    }

    private void onEditAccount() {
        getSelectedAccount().flatMap(account -> new AccountDialog(this, account, null)
            .showAndWait())
            .ifPresent(account -> {
                getDao().updateAccount(account);
                tableView.scrollTo(account);
                tableView.getSelectionModel().select(account);
            });
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
            boolean enabled = account.enabled();
            getDao().updateAccount(account.enable(!enabled));
        });
    }

    private void onCopyName() {
        getSelectedAccount().map(Account::name).ifPresent(name -> {
            Clipboard cb = Clipboard.getSystemClipboard();
            ClipboardContent ct = new ClipboardContent();
            ct.putString(name);
            cb.setContent(ct);
        });
    }

    private void onShowTransactions() {
        getSelectedAccount().ifPresent(BaseController::getRequestController);
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
