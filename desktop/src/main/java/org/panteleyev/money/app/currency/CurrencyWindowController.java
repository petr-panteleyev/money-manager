/*
 Copyright © 2020-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.currency;

import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.model.Currency;

import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

public final class CurrencyWindowController extends BaseController {
    private final SortedList<Currency> sortedList = cache().getCurrencies().sorted();
    private final TableView<Currency> table = new TableView<>(sortedList);

    public CurrencyWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCurrency, this::onEditCurrency, this::onDeleteCurrency,
                table.getSelectionModel().selectedItemProperty().isNull()
        );

        var menuBar = menuBar(
                newMenu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                newMenu("Правка",
                        createMenuItem(crudActionsHolder.getCreateAction()),
                        createMenuItem(crudActionsHolder.getUpdateAction()),
                        new SeparatorMenuItem(),
                        createMenuItem(crudActionsHolder.getDeleteAction())
                ),
                createWindowMenu(),
                createHelpMenu()
        );
        menuBar.getMenus().forEach(menu -> menu.disableProperty().bind(getStage().focusedProperty().not()));

        setupCurrencyColumns();

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createMenuItem(crudActionsHolder.getUpdateAction()),
                new SeparatorMenuItem(),
                createContextMenuItem(crudActionsHolder.getDeleteAction())
        ));

        var root = new BorderPane(
                new BorderPane(table, null, null, null, null),
                menuBar, null, null, null
        );

        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Валюты";
    }

    private Optional<Currency> getSelectedCurrency() {
        return Optional.ofNullable(table.getSelectionModel().getSelectedItem());
    }

    private void onCreateCurrency(ActionEvent event) {
        new CurrencyDialog(this, settings().getDialogCssFileUrl(), null)
                .showAndWait()
                .ifPresent(c -> dao().insertCurrency(c));
    }

    private void onEditCurrency(ActionEvent event) {
        getSelectedCurrency().flatMap(selected ->
                        new CurrencyDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
                .ifPresent(c -> dao().updateCurrency(c));
    }

    private void onDeleteCurrency(ActionEvent ignored) {
        getSelectedCurrency().ifPresent(currency ->
                new Alert(Alert.AlertType.CONFIRMATION, "Удалить эту валюту?")
                        .showAndWait()
                        .ifPresent(r -> {
                            if (r == ButtonType.OK) {
                                dao().deleteCurrency(currency);
                            }
                        }));
    }

    private void setupCurrencyColumns() {
        var w = table.widthProperty().subtract(20);
        TableColumn<Currency, String> nameColumn = tableColumn("Название", b ->
                b.withPropertyCallback(Currency::symbol)
                        .withComparator(String::compareTo)
                        .withWidthBinding(w.multiply(0.2)));

        table.getColumns().setAll(List.of(
                nameColumn,
                tableColumn("Описание", b ->
                        b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));
        sortedList.comparatorProperty().bind(table.comparatorProperty());
        table.getSortOrder().add(nameColumn);
        table.sort();
    }
}
