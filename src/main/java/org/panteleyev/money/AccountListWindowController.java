/*
 * Copyright (c) 2017, Petr Panteleyev <petr@panteleyev.org>
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

import java.math.BigDecimal;
import java.net.URL;
import java.util.Collection;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.panteleyev.money.persistence.Account;
import org.panteleyev.money.persistence.Category;
import org.panteleyev.money.persistence.CategoryType;
import org.panteleyev.money.persistence.Currency;
import org.panteleyev.money.persistence.MoneyDAO;
import org.panteleyev.money.persistence.ReadOnlyStringConverter;

public class AccountListWindowController extends BaseController implements Initializable {
    private static final String FXML = "/org/panteleyev/money/AccountListWindow.fxml";

    private @FXML ChoiceBox             typeChoiceBox;
    private @FXML ChoiceBox             categoryChoiceBox;
    private @FXML CheckBox              showActiveCheckBox;
    private @FXML TableView<Account>    accountListTable;

    private @FXML TableColumn<Account, Integer>     idColumn;
    private @FXML TableColumn<Account, String>      nameColumn;
    private @FXML TableColumn<Account, String>      typeColumn;
    private @FXML TableColumn<Account, String>      categoryColumn;
    private @FXML TableColumn<Account, String>      currencyColumn;
    private @FXML TableColumn<Account, BigDecimal>  balanceColumn;
    private @FXML TableColumn<Account, CheckBox>    activeColumn;

    // Main menu
    private @FXML MenuItem  addMenuItem;
    private @FXML MenuItem  editMenuItem;
    private @FXML MenuItem  deleteMenuItem;
    // Context menu
    private @FXML MenuItem  ctxAddMenuItem;
    private @FXML MenuItem  ctxEditMenuItem;
    private @FXML MenuItem  ctxDeleteMenuItem;

    private @FXML Parent    self;

    private ResourceBundle  bundle;

    private final SimpleMapProperty<Integer, Account> accountsProperty =
        new SimpleMapProperty<>();


    public AccountListWindowController() {
        super(FXML, MainWindowController.UI_BUNDLE_PATH, true);

        accountsProperty.bind(MoneyDAO.getInstance().accountsProperty());
    }

    @Override
    public String getTitle() {
        return bundle == null? "Accounts" : bundle.getString("account.Window.Title");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bundle = rb;

        MoneyDAO dao = MoneyDAO.getInstance();

        ObservableList types = FXCollections.observableArrayList(dao.getCategoryTypes());
        if (!types.isEmpty()) {
            types.add(0, new Separator());
        }
        types.add(0, rb.getString("account.Window.AllTypes"));

        typeChoiceBox.setItems(types);
        typeChoiceBox.getSelectionModel().select(0);

        typeChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object object) {
                if (object instanceof CategoryType) {
                    return ((CategoryType)object).getTranslatedName();
                } else {
                    return (object == null)? "-" : object.toString();
                }
            }
        });

        typeChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            onTypeChanged(newValue);
        });

        categoryChoiceBox.setConverter(new ReadOnlyStringConverter() {
            @Override
            public String toString(Object object) {
                if (object instanceof Category) {
                    return ((Category)object).getName();
                } else {
                    return (object == null)? "-" : object.toString();
                }
            }
        });

        categoryChoiceBox.setItems(FXCollections.observableArrayList(rb.getString("account.Window.AllCategories")));
        categoryChoiceBox.getSelectionModel().select(0);
        categoryChoiceBox.valueProperty().addListener((observable,oldValue,newValue) ->
            reloadAccounts());

        showActiveCheckBox.setOnAction(event -> reloadAccounts());

        idColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, Integer> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getId()));
        nameColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper(p.getValue().getName()));

        typeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper(dao.getCategoryType(p.getValue().getTypeId())
                        .map(CategoryType::getTranslatedName)
                        .orElse(""))
        );
        categoryColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper(dao.getCategory(p.getValue().getCategoryId())
                        .map(Category::getName)
                        .orElse(""))
        );
        currencyColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, String> p) ->
                new ReadOnlyObjectWrapper(dao.getCurrency(p.getValue().getCurrencyId().orElse(null))
                        .map(Currency::getSymbol)
                        .orElse(""))
        );
        balanceColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, BigDecimal> p) ->
            new ReadOnlyObjectWrapper(p.getValue().getOpeningBalance().setScale(2, BigDecimal.ROUND_HALF_UP))
        );
        activeColumn.setCellValueFactory((TableColumn.CellDataFeatures<Account, CheckBox> p) -> {
            Account account = p.getValue();

            CheckBox cb = new CheckBox();
            cb.setSelected(p.getValue().isEnabled());

            cb.setOnAction(event -> {
                boolean enabled = account.isEnabled();
                dao.updateAccount(account.enable(!enabled));
            });

            return new ReadOnlyObjectWrapper(cb);
        });

        editMenuItem.disableProperty()
            .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        deleteMenuItem.disableProperty()
            .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        ctxEditMenuItem.disableProperty()
            .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());
        ctxDeleteMenuItem.disableProperty()
            .bind(accountListTable.getSelectionModel().selectedItemProperty().isNull());

        accountsProperty.addListener((x, y, z) -> Platform.runLater(this::reloadAccounts));

        reloadAccounts();
    }

    private void reloadAccounts() {
        Collection<Account> accounts;

        MoneyDAO dao = MoneyDAO.getInstance();

        Object catObject = categoryChoiceBox.getSelectionModel().getSelectedItem();
        if (catObject instanceof Category) {
            accounts = dao.getAccountsByCategory(((Category)catObject).getId());
        } else {
            Object typeObject = typeChoiceBox.getSelectionModel().getSelectedItem();
            if (typeObject instanceof CategoryType) {
                accounts = dao.getAccountsByType(((CategoryType)typeObject).getId());
            } else {
                accounts = dao.getAccounts();
            }
        }

        if (showActiveCheckBox.isSelected()) {
            accounts = accounts.stream()
                .filter(a -> a.isEnabled())
                .collect(Collectors.toList());
        }

        accountListTable.setItems(FXCollections.observableArrayList(accounts));
    }

    public void onTypeChanged(Object newValue) {
        ObservableList items;

        if (newValue instanceof String) {
            items = FXCollections.observableArrayList(bundle.getString("account.Window.AllCategories"));
        } else {
            items = FXCollections.observableArrayList(
                MoneyDAO.getInstance().getCategoriesByType((CategoryType)newValue)
            );

            if (!items.isEmpty()) {
                items.add(0, new Separator());
            }
            items.add(0, bundle.getString("account.Window.AllCategories"));
        }

        categoryChoiceBox.setItems(items);
        categoryChoiceBox.getSelectionModel().select(0);
    }

    public void onAddAccount() {
        MoneyDAO dao = MoneyDAO.getInstance();

        new AccountDialog((Account)null).load().showAndWait().ifPresent(builder -> {
            MoneyDAO.getInstance().insertAccount(builder
                    .id(dao.generatePrimaryKey(Account.class))
                    .build());
        });
    }

    private Optional<Account> getSelectedAccount() {
        return Optional.of(accountListTable.getSelectionModel().getSelectedItem());
    }

    public void onEditAccount() {
        getSelectedAccount().ifPresent(account -> {
            new AccountDialog(account).load().showAndWait().ifPresent(builder -> {
                MoneyDAO.getInstance().updateAccount(builder.build());
            });
        });
    }

    public void onDeleteAccount() {
        getSelectedAccount().ifPresent(account -> {
            long count = MoneyDAO.getInstance().getTransactionCount(account);
            if (count != 0) {
                new Alert(Alert.AlertType.ERROR, "Unable to delete account\nwith " + count + " associated transactions", ButtonType.CLOSE)
                    .showAndWait();
            } else {
                new Alert(Alert.AlertType.CONFIRMATION, bundle.getString("text.AreYouSure"), ButtonType.OK, ButtonType.CANCEL).showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> {
                        MoneyDAO.getInstance().deleteAccount(account);
                    });
            }
        });
    }

    @Override
    protected Parent getSelf() {
        return self;
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
