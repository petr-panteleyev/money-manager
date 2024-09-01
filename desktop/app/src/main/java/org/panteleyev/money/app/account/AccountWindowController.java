/*
 Copyright © 2017-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
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
import org.controlsfx.control.action.Action;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.Comparators;
import org.panteleyev.money.app.ReportType;
import org.panteleyev.money.app.Reports;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.dialogs.ReportFileDialog;
import org.panteleyev.money.app.filters.AccountNameFilterBox;
import org.panteleyev.money.app.filters.CategorySelectionBox;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Category;
import org.panteleyev.money.model.Transaction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.MenuFactory.checkMenuItem;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Predicates.activeAccount;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_ALT_R;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_C;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_H;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_R;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_T;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.actions.ActionBuilder.actionBuilder;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class AccountWindowController extends BaseController {
    // Filters
    private final CategorySelectionBox categorySelectionBox = new CategorySelectionBox();
    private final AccountNameFilterBox accountNameFilterBox = new AccountNameFilterBox();
    private final PredicateProperty<Account> showDeactivatedAccounts =
            new PredicateProperty<>(settings().getShowDeactivatedAccounts() ? _ -> true : activeAccount(true));

    private final PredicateProperty<Account> filterProperty =
            PredicateProperty.and(List.of(
                    categorySelectionBox.accountFilterProperty(),
                    accountNameFilterBox.predicateProperty(),
                    showDeactivatedAccounts
            ));

    // Items
    private final FilteredList<Account> filteredAccounts = cache().getAccounts().filtered(filterProperty.get());
    private final TableView<Account> tableView = new AccountTableView(filteredAccounts.sorted());

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Category> categoryListener = _ -> Platform.runLater(() -> {
        tableView.getColumns().getFirst().setVisible(false);
        tableView.getColumns().getFirst().setVisible(true);
    });

    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<Transaction> transactionListener = _ -> Platform.runLater(tableView::refresh);

    // Actions
    private final BooleanBinding disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();

    private final CrudActionsHolder crudActionsHolder = new CrudActionsHolder(
            this::onNewAccount, this::onEditAccount, this::onDeleteAccount, disableBinding
    );

    private final Action searchAction = searchAction(_ -> accountNameFilterBox.getTextField().requestFocus());
    private final Action attachDocumentAction = actionBuilder("Прикрепить документ...", this::onAttachDocument)
            .disableBinding(disableBinding).build();
    private final Action documentsAction = actionBuilder("Документы...", this::onDocuments)
            .disableBinding(disableBinding).build();
    private final Action copyNameAction = actionBuilder("Копировать название", this::onCopyName)
            .accelerator(SHORTCUT_C).disableBinding(disableBinding).build();
    private final Action transactionsAction = actionBuilder("Проводки...", this::onShowTransactions)
            .accelerator(SHORTCUT_T).disableBinding(disableBinding).build();
    private final Action refreshBalanceAction = actionBuilder("Пересчитать баланс", this::onUpdateBalance)
            .accelerator(SHORTCUT_R).build();

    public AccountWindowController() {
        createContextMenu();

        // Tool box
        var hBox = hBox(5.0,
                accountNameFilterBox.getTextField(),
                categorySelectionBox
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        var self = new BorderPane(
                new BorderPane(tableView, hBox, null, null, null),
                createMainMenu(), null, null, null
        );

        BorderPane.setMargin(hBox, BIG_INSETS);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        categorySelectionBox.setupCategoryTypesBox();

        filteredAccounts.predicateProperty().bind(filterProperty);

        cache().getCategories().addListener(new WeakListChangeListener<>(categoryListener));
        cache().getTransactions().addListener(new WeakListChangeListener<>(transactionListener));

        setupWindow(self);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Счета";
    }

    private MenuBar createMainMenu() {
        var disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();

        var activateAccountMenuItem = menuItem("Деактивировать",
                _ -> onActivateDeactivateAccount(),
                disableBinding);

        var editMenu = menu("Правка",
                createMenuItem(crudActionsHolder.getCreateAction()),
                createMenuItem(crudActionsHolder.getUpdateAction()),
                new SeparatorMenuItem(),
                createMenuItem(crudActionsHolder.getDeleteAction()),
                new SeparatorMenuItem(),
                createMenuItem(copyNameAction),
                activateAccountMenuItem,
                new SeparatorMenuItem(),
                createMenuItem(searchAction),
                new SeparatorMenuItem(),
                createMenuItem(transactionsAction),
                new SeparatorMenuItem(),
                createMenuItem(attachDocumentAction),
                createMenuItem(documentsAction),
                new SeparatorMenuItem(),
                createMenuItem(refreshBalanceAction)
        );

        editMenu.setOnShowing(_ -> getSelectedAccount()
                .ifPresent(account -> activateAccountMenuItem.setText(
                        account.enabled() ? "Деактивировать" : "Активировать")
                )
        );

        var menuBar = menuBar(
                menu("Файл",
                        menuItem("Отчет...", SHORTCUT_ALT_R, _ -> onReport()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)),
                editMenu,
                menu("Вид",
                        checkMenuItem("Показывать неактивные счета",
                                settings().getShowDeactivatedAccounts(), SHORTCUT_H,
                                event -> {
                                    var selected = ((CheckMenuItem) event.getSource()).isSelected();
                                    settings().update(opt -> opt.setShowDeactivatedAccounts(selected));
                                    showDeactivatedAccounts.set(selected ? _ -> true : activeAccount(true));
                                }
                        )
                ),
                createPortfolioMenu(),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));
        return menuBar;
    }

    private void createContextMenu() {
        var disableBinding = tableView.getSelectionModel().selectedItemProperty().isNull();

        var activateAccountMenuItem = menuItem("Деактивировать",
                _ -> onActivateDeactivateAccount(),
                disableBinding);

        var contextMenu = new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createContextMenuItem(crudActionsHolder.getUpdateAction()),
                new SeparatorMenuItem(),
                createContextMenuItem(crudActionsHolder.getDeleteAction()),
                new SeparatorMenuItem(),
                createContextMenuItem(attachDocumentAction),
                createContextMenuItem(documentsAction),
                new SeparatorMenuItem(),
                createContextMenuItem(copyNameAction),
                activateAccountMenuItem,
                new SeparatorMenuItem(),
                createContextMenuItem(searchAction),
                new SeparatorMenuItem(),
                createContextMenuItem(transactionsAction),
                new SeparatorMenuItem(),
                createContextMenuItem(refreshBalanceAction)
        );

        contextMenu.setOnShowing(_ -> getSelectedAccount()
                .ifPresent(account -> activateAccountMenuItem.setText(
                        account.enabled() ? "Деактивировать" : "Активировать")
                )
        );

        tableView.setContextMenu(contextMenu);
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private void onNewAccount(ActionEvent ignored) {
        var initialCategory = getSelectedAccount()
                .flatMap(account -> cache().getCategory(account.categoryUuid()))
                .orElse(null);

        new AccountDialog(this, settings().getDialogCssFileUrl(), initialCategory).showAndWait()
                .ifPresent(account -> {
                    dao().insertAccount(account);
                    tableView.scrollTo(account);
                    tableView.getSelectionModel().select(account);
                });
    }

    private void onEditAccount(ActionEvent ignored) {
        getSelectedAccount().flatMap(selected ->
                        new AccountDialog(this, settings().getDialogCssFileUrl(), selected, null)
                                .showAndWait())
                .ifPresent(account -> {
                    dao().updateAccount(account);
                    tableView.scrollTo(account);
                    tableView.getSelectionModel().select(account);
                });
    }

    private void onDeleteAccount(ActionEvent ignored) {
        getSelectedAccount().ifPresent(account -> {
            long count = cache().getTransactionCount(account);
            if (count != 0L) {
                new Alert(Alert.AlertType.ERROR,
                        "Unable to delete account\nwith " + count + " associated transactions",
                        ButtonType.CLOSE).showAndWait();
            } else {
                new Alert(Alert.AlertType.CONFIRMATION, "Вы уверены?", ButtonType.OK,
                        ButtonType.CANCEL)
                        .showAndWait()
                        .filter(response -> response == ButtonType.OK)
                        .ifPresent(_ -> dao().deleteAccount(account));
            }
        });
    }

    private void onActivateDeactivateAccount() {
        getSelectedAccount().ifPresent(account -> {
            boolean enabled = account.enabled();
            dao().updateAccount(account.enable(!enabled));
        });
    }

    private void onCopyName(ActionEvent ignored) {
        getSelectedAccount().map(Account::name).ifPresent(name -> {
            Clipboard cb = Clipboard.getSystemClipboard();
            ClipboardContent ct = new ClipboardContent();
            ct.putString(name);
            cb.setContent(ct);
        });
    }

    private void onShowTransactions(ActionEvent ignored) {
        getSelectedAccount().ifPresent(BaseController::getRequestController);
    }

    private void onReport() {
        new ReportFileDialog().show(getStage(), ReportType.ACCOUNTS).ifPresent(selected -> {
            try (var outputStream = new FileOutputStream(selected)) {
                var accounts = cache().getAccounts(filterProperty.get())
                        .sorted(Comparators.accountsByCategory(cache())
                                .thenComparing(Comparators.accountsByName()))
                        .toList();
                Reports.reportAccounts(accounts, outputStream);
                settings().update(opt -> opt.setLastReportDir(selected.getParent()));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });
    }

    private void onUpdateBalance(ActionEvent ignored) {
        tableView.getItems().forEach(account -> {
            var total = cache().calculateBalance(account, false, _ -> true);
            var waiting = cache().calculateBalance(account, false, t -> !t.checked());
            dao().updateAccount(account.updateBalance(total, waiting));
        });
    }

    private void onDocuments(ActionEvent ignored) {
        getSelectedAccount().ifPresent(BaseController::getDocumentController);
    }

    private void onAttachDocument(ActionEvent event) {
        getSelectedAccount().ifPresent(account -> {
            var controller = getDocumentController(account);
            controller.onCreateDocument(event);
        });
    }
}
