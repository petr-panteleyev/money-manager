// Copyright © 2023-2026 Petr Panteleyev
// SPDX-License-Identifier: BSD-2-Clause
package org.panteleyev.money.app.exchange;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.fx.FxAction;
import org.panteleyev.fx.ToStringConverter;
import org.panteleyev.fx.factories.TableFactory;
import org.panteleyev.moex.Moex;
import org.panteleyev.moex.model.MoexMarketData;
import org.panteleyev.moex.model.MoexSecurity;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.cells.ExchangeSecurityValueCell;
import org.panteleyev.money.app.exchange.cells.ExchangeTypeCell;
import org.panteleyev.money.model.ExchangeSecurity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.panteleyev.fx.FxAction.fxAction;
import static org.panteleyev.fx.factories.BoxFactory.hBox;
import static org.panteleyev.fx.factories.ChoiceBoxFactory.choiceBox;
import static org.panteleyev.fx.factories.MenuFactory.menu;
import static org.panteleyev.fx.factories.MenuFactory.menuBar;
import static org.panteleyev.fx.factories.MenuFactory.menuItem;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;
import static org.panteleyev.money.app.Styles.BIG_INSETS;

public class SecuritiesWindowController extends BaseController {
    private record ExchangeGroup(String type, String title) {
    }

    private static final ExchangeGroup ALL_ITEMS = new ExchangeGroup("", "Все типы");

    private final ChoiceBox<Object> groupBox = groupBox();

    private final FilteredList<ExchangeSecurity> filteredList = cache().getExchangeSecurities().filtered(_ -> true);
    private final SortedList<ExchangeSecurity> sortedList = filteredList.sorted();

    private final TableView<ExchangeSecurity> tableView = new TableView<>(sortedList);

    private final FxAction addSecurityAction = fxAction("Добавить...")
            .onAction(this::onAddSecurity)
            .accelerator(SHORTCUT_N);
    private final FxAction updateSecurityAction = fxAction("Обновить...")
            .onAction(this::onUpdateSecurity)
            .accelerator(SHORTCUT_U);
    private final FxAction splitsAction = fxAction("Сплиты...")
            .onAction(this::onSplits);

    private final Moex moex = new Moex();

    public SecuritiesWindowController() {
        setupTable();

        updateSecurityAction.disableProperty().bind(
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );
        splitsAction.disableProperty().bind(
                tableView.getSelectionModel().selectedItemProperty().isNull()
        );

        var toolBar = hBox(5, groupBox);
        BorderPane.setMargin(toolBar, BIG_INSETS);

        var root = new BorderPane(
                new BorderPane(tableView, toolBar, null, null, null),
                createMenuBar(), null, null, null
        );
        root.setPrefSize(600.0, 400.0);

        groupBox.getSelectionModel().select(0);
        groupBox.valueProperty().addListener((_, _, _) -> updatePredicate());
        setupGroupBox();

        tableView.setContextMenu(createContextMenu());

        setupWindow(root);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Ценные бумаги";
    }

    private void setupTable() {
        var w = tableView.widthProperty().subtract(20);

        var codeColumn = TableFactory.<ExchangeSecurity>tableStringColumn("Код");
        codeColumn.valueConverter(ExchangeSecurity::secId);
        codeColumn.comparator(String::compareTo);
        codeColumn.widthBinding(w.multiply(0.1));

        var typeColumn = TableFactory.<ExchangeSecurity>tableObjectColumn("Тип");
        typeColumn.setCellFactory(_ -> new ExchangeTypeCell());
        typeColumn.comparator(Comparator.comparing(ExchangeSecurity::typeName)
                .thenComparing(ExchangeSecurity::secId));
        typeColumn.widthBinding(w.multiply(0.2));

        var shortNameColumn = TableFactory.<ExchangeSecurity>tableStringColumn("Название");
        shortNameColumn.valueConverter(ExchangeSecurity::shortName);
        shortNameColumn.widthBinding(w.multiply(0.1));

        var fullNameColumn = TableFactory.<ExchangeSecurity>tableStringColumn("Полное название");
        fullNameColumn.valueConverter(ExchangeSecurity::name);
        fullNameColumn.widthBinding(w.multiply(0.3));

        var isinColumn = TableFactory.<ExchangeSecurity>tableStringColumn("ISIN");
        isinColumn.valueConverter(ExchangeSecurity::isin);
        isinColumn.widthBinding(w.multiply(0.1));

        var regColumn = TableFactory.<ExchangeSecurity>tableStringColumn("Гос. регистрация");
        regColumn.valueConverter(ExchangeSecurity::regNumber);
        regColumn.widthBinding(w.multiply(0.1));

        var valueColumn = TableFactory.<ExchangeSecurity>tableObjectColumn("Стоимость");
        valueColumn.setCellFactory(_ -> new ExchangeSecurityValueCell());
        valueColumn.widthBinding(w.multiply(0.1));

        tableView.getColumns().setAll(List.of(codeColumn, typeColumn, shortNameColumn, fullNameColumn,
                isinColumn, regColumn, valueColumn));
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.getSortOrder().add(codeColumn);
        tableView.sort();
    }

    private MenuBar createMenuBar() {
        return menuBar(
                menu("Файл", ACTION_CLOSE.createMenuItem()),
                menu("Правка",
                        addSecurityAction.createMenuItem(),
                        updateSecurityAction.createMenuItem(),
                        new SeparatorMenuItem(),
                        menuItem("Обновить все котировки", this::onUpdateAllValues)
                ),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private ContextMenu createContextMenu() {
        return new ContextMenu(
                addSecurityAction.createMenuItem(),
                updateSecurityAction.createMenuItem(),
                new SeparatorMenuItem(),
                splitsAction.createMenuItem()
        );
    }

    private void onAddSecurity(ActionEvent ignored) {
        new SecurityCodeDialog(this).showAndWait()
                .flatMap(moex::getSecurity).ifPresent(moexSecurity -> {
                    var security = buildExchangeSecurity(moexSecurity, null);

                    new ExchangeSecurityDialog(this, security).showAndWait().ifPresent(_ -> {
                        dao().insertExchangeSecurity(security);
                        setupGroupBox();
                    });
                });
    }

    private ExchangeSecurity buildExchangeSecurity(MoexSecurity moexSecurity, UUID uuid) {
        var marketData = getMarketData(moexSecurity);

        var marketValue = marketData.map(MoexMarketData::prevLegalClosePrice).orElse(null);
        if (marketValue == null) {
            marketValue = marketData.map(MoexMarketData::marketPrice).orElse(BigDecimal.ZERO);
        }

        var builder = new ExchangeSecurity.Builder()
                .secId(moexSecurity.secId())
                .name(moexSecurity.name())
                .shortName(moexSecurity.shortName())
                .isin(moexSecurity.isin())
                .regNumber(moexSecurity.regNumber())
                .faceValue(moexSecurity.faceValue())
                .issueDate(moexSecurity.issueDate())
                .matDate(moexSecurity.matDate())
                .daysToRedemption(moexSecurity.daysToRedemption())
                .group(moexSecurity.group())
                .groupName(moexSecurity.groupName())
                .type(moexSecurity.type())
                .typeName(moexSecurity.typeName())
                .marketValue(marketValue)
                .couponValue(moexSecurity.couponValue())
                .couponPercent(moexSecurity.couponPercent())
                .couponDate(moexSecurity.couponDate())
                .couponFrequency(moexSecurity.couponFrequency())
                .accruedInterest(marketData
                        .map(MoexMarketData::accruedInterest)
                        .orElse(null))
                .couponPeriod(marketData
                        .map(MoexMarketData::couponPeriod)
                        .orElse(null));

        if (uuid != null) {
            builder.uuid(uuid);
        }
        return builder.build();
    }

    private Optional<MoexMarketData> getMarketData(MoexSecurity security) {
        return moex.getMarketData(security.secId(), security.engine(), security.market(), security.primaryBoard())
                .stream()
                .findFirst();
    }

    private void onUpdateSecurity(ActionEvent ignored) {
        getSelected().ifPresent(selected -> moex.getSecurity(selected.secId()).ifPresent(moexSecurity -> {
            var updated = buildExchangeSecurity(moexSecurity, selected.uuid());
            new ExchangeSecurityDialog(this, updated).showAndWait().ifPresent(_ -> {
                dao().updateExchangeSecurity(updated);
                setupGroupBox();
            });
        }));
    }

    private void onSplits(ActionEvent ignored) {
        getSelected().ifPresent(selected -> new ExchangeSecuritySplitsDialog(this, selected).showAndWait());
    }

    private void onUpdateAllValues(ActionEvent ignored) {
        cache().getExchangeSecurities()
                .forEach(sec -> CompletableFuture.supplyAsync(() -> moex.getSecurity(sec.secId()).orElse(null))
                        .thenAcceptAsync(moexSecurity -> {
                            var updated = buildExchangeSecurity(moexSecurity, sec.uuid());
                            dao().updateExchangeSecurity(updated);
                        }, Platform::runLater));
    }

    private void updatePredicate() {
        var selected = groupBox.getSelectionModel().getSelectedItem();
        if (selected instanceof ExchangeGroup group) {
            if (group == ALL_ITEMS) {
                filteredList.setPredicate(_ -> true);
            } else {
                filteredList.setPredicate(security -> Objects.equals(security.group(), group.type()));
            }
        }
    }

    private void setupGroupBox() {
        groupBox.getItems().setAll(ALL_ITEMS);

        var groups = cache().getExchangeSecurities().stream()
                .map(e -> new ExchangeGroup(e.group(), e.groupName()))
                .distinct()
                .sorted(Comparator.comparing(ExchangeGroup::title))
                .toList();

        if (!groups.isEmpty()) {
            groupBox.getItems().add(new Separator());
            groupBox.getItems().addAll(groups);
        }
        groupBox.getSelectionModel().selectFirst();
    }

    private Optional<ExchangeSecurity> getSelected() {
        return Optional.ofNullable(tableView.getSelectionModel().getSelectedItem());
    }

    private ChoiceBox<Object> groupBox() {
        var cb = choiceBox(List.of());
        cb.setOnAction(_ -> updatePredicate());
        cb.setConverter(new ToStringConverter<>() {
            @Override
            public String toString(Object object) {
                if (object instanceof ExchangeGroup group) {
                    return group.title();
                } else {
                    return "";
                }
            }
        });
        return cb;
    }
}
