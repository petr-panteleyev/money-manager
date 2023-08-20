/*
 Copyright © 2020-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app;

import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.money.app.actions.CrudActionsHolder;
import org.panteleyev.money.app.cells.CurrencyRateCell;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.CurrencyType;

import java.util.List;
import java.util.Optional;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.newMenu;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.choicebox.ChoiceBoxBuilder.choiceBox;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.util.MenuUtils.createContextMenuItem;

final class CurrencyWindowController extends BaseController {

    private final FilteredList<Currency> filteredList = cache().getCurrencies().filtered(
            c -> c.type() == CurrencyType.CURRENCY
    );

    private final TableView<Currency> table = new TableView<>(filteredList);
    private final ChoiceBox<CurrencyType> typeChoiceBox = choiceBox(CurrencyType.values(), b -> {
        b.withStringConverter(Bundles::translate);
    });

    CurrencyWindowController() {
        var crudActionsHolder = new CrudActionsHolder(
                this::onCreateCurrency, this::onEditCurrency, e -> {},
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

        // toolbox
        var hBox = hBox(5.0,
                label("Тип:"),
                typeChoiceBox
        );
        hBox.setAlignment(Pos.CENTER_LEFT);

        // Context Menu
        table.setContextMenu(new ContextMenu(
                createContextMenuItem(crudActionsHolder.getCreateAction()),
                createMenuItem(crudActionsHolder.getUpdateAction()),
                new SeparatorMenuItem(),
                createContextMenuItem(crudActionsHolder.getDeleteAction())
        ));

        var root = new BorderPane(
                new BorderPane(table, hBox, null, null, null),
                menuBar, null, null, null
        );

        setupWindow(root);

        typeChoiceBox.setOnAction(this::onTypeSelected);
        typeChoiceBox.setValue(CurrencyType.CURRENCY);

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
        switch (typeChoiceBox.getValue()) {
            case CURRENCY -> new CurrencyDialog(this, settings().getDialogCssFileUrl(), null)
                    .showAndWait()
                    .ifPresent(c -> dao().insertCurrency(c));

            case SECURITY -> new SecurityDialog(this, settings().getDialogCssFileUrl(), null)
                    .showAndWait()
                    .ifPresent(c -> dao().insertCurrency(c));
        }
    }

    private void onEditCurrency(ActionEvent event) {
        switch (typeChoiceBox.getValue()) {
            case CURRENCY -> getSelectedCurrency()
                    .flatMap(selected ->
                            new CurrencyDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
                    .ifPresent(c -> dao().updateCurrency(c));
            case SECURITY -> getSelectedCurrency()
                    .flatMap(selected ->
                            new SecurityDialog(this, settings().getDialogCssFileUrl(), selected).showAndWait())
                    .ifPresent(c -> dao().updateCurrency(c));
        }
    }

    private void onTypeSelected(ActionEvent ignored) {
        filteredList.setPredicate(c -> c.type() == typeChoiceBox.getValue());

        switch (typeChoiceBox.getValue()) {
            case CURRENCY -> setupCurrencyColumns();
            case SECURITY -> setupSecurityColumns();
        }
    }

    private void setupCurrencyColumns() {
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn("Название", b ->
                        b.withPropertyCallback(Currency::symbol).withWidthBinding(w.multiply(0.2))),
                tableColumn("Описание", b ->
                        b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.8)))
        ));
    }

    private void setupSecurityColumns() {
        var w = table.widthProperty().subtract(20);
        table.getColumns().setAll(List.of(
                tableColumn("Полное наименование", b ->
                        b.withPropertyCallback(Currency::description).withWidthBinding(w.multiply(0.4))),
                tableColumn("Аббревиатура", b ->
                        b.withPropertyCallback(Currency::symbol).withWidthBinding(w.multiply(0.1))),
                tableColumn("ISIN", b ->
                        b.withPropertyCallback(Currency::isin).withWidthBinding(w.multiply(0.2))),
                tableColumn("Гос. регистрация", b ->
                        b.withPropertyCallback(Currency::registry).withWidthBinding(w.multiply(0.1))),
                tableObjectColumn("Котировка", b ->
                        b.withCellFactory(c -> new CurrencyRateCell()).withWidthBinding(w.multiply(0.2)))
        ));
    }
}
