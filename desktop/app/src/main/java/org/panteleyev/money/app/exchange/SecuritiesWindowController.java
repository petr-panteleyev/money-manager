/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.exchange;

import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import org.panteleyev.moex.Moex;
import org.panteleyev.moex.model.MoexMarketData;
import org.panteleyev.moex.model.MoexSecurity;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.cells.ExchangeSecurityValueCell;
import org.panteleyev.money.app.exchange.cells.ExchangeTypeCell;
import org.panteleyev.money.model.exchange.ExchangeSecurity;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.TableColumnBuilder.tableColumn;
import static org.panteleyev.fx.TableColumnBuilder.tableObjectColumn;
import static org.panteleyev.fx.choicebox.ChoiceBoxBuilder.choiceBox;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_N;
import static org.panteleyev.money.app.Shortcuts.SHORTCUT_U;

public class SecuritiesWindowController extends BaseController {
    private record ExchangeGroup(String type, String title) {
    }

    private static final ExchangeGroup ALL_ITEMS = new ExchangeGroup("", "Все типы");

    private final ChoiceBox<Object> groupBox = choiceBox(List.of(),
            b -> b.withHandler(_ -> updatePredicate())
                    .withStringConverter(object -> {
                        if (object instanceof ExchangeGroup group) {
                            return group.title();
                        } else {
                            return "";
                        }
                    })
    );

    private final FilteredList<ExchangeSecurity> filteredList = cache().getExchangeSecurities().filtered(_ -> true);
    private final SortedList<ExchangeSecurity> sortedList = filteredList.sorted();

    private final TableView<ExchangeSecurity> tableView = new TableView<>(sortedList);


    private final Moex moex = new Moex();

    public SecuritiesWindowController() {
        // Toolbox
        var hBox = hBox(5, groupBox);
        BorderPane.setMargin(hBox, new Insets(5.0, 5.0, 5.0, 5.0));

        setupTable();

        var root = new BorderPane(
                new BorderPane(tableView, hBox, null, null, null),
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

        TableColumn<ExchangeSecurity, String> codeColumn = tableColumn("Код", b ->
                b.withPropertyCallback(ExchangeSecurity::secId)
                        .withComparator(String::compareTo)
                        .withWidthBinding(w.multiply(0.1)));

        tableView.getColumns().setAll(List.of(
                codeColumn,
                tableObjectColumn("Тип", b ->
                        b.withCellFactory(_ -> new ExchangeTypeCell())
                                .withComparator(Comparator.comparing(ExchangeSecurity::typeName)
                                        .thenComparing(ExchangeSecurity::secId))
                                .withWidthBinding(w.multiply(0.2))),
                tableColumn("Название", b ->
                        b.withPropertyCallback(ExchangeSecurity::shortName).withWidthBinding(w.multiply(0.1))),
                tableColumn("Полное название", b ->
                        b.withPropertyCallback(ExchangeSecurity::name).withWidthBinding(w.multiply(0.3))),
                tableColumn("ISIN", b ->
                        b.withPropertyCallback(ExchangeSecurity::isin).withWidthBinding(w.multiply(0.1))),
                tableColumn("Гос. регистрация", b ->
                        b.withPropertyCallback(ExchangeSecurity::regNumber).withWidthBinding(w.multiply(0.1))),
                tableObjectColumn("Стоимость", b ->
                        b.withCellFactory(_ -> new ExchangeSecurityValueCell()).withWidthBinding(w.multiply(0.1)))
        ));
        sortedList.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.getSortOrder().add(codeColumn);
        tableView.sort();
    }

    private MenuBar createMenuBar() {
        return menuBar(
                menu("Файл",
                        createMenuItem(ACTION_CLOSE)
                ),
                menu("Правка",
                        menuItem("Добавить...", SHORTCUT_N, this::onAddSecurity),
                        menuItem("Обновить...", SHORTCUT_U, this::onUpdateSecurity,
                                tableView.getSelectionModel().selectedItemProperty().isNull()),
                        new SeparatorMenuItem(),
                        menuItem("Обновить все котировки", this::onUpdateAllValues)
                ),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private ContextMenu createContextMenu() {
        return new ContextMenu(
                menuItem("Добавить...", this::onAddSecurity),
                menuItem("Обновить...", this::onUpdateSecurity,
                        tableView.getSelectionModel().selectedItemProperty().isNull()),
                new SeparatorMenuItem(),
                menuItem("Сплиты...", this::onSplits,
                        tableView.getSelectionModel().selectedItemProperty().isNull())
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
                .marketValue(marketData
                        .map(MoexMarketData::last)
                        .orElse(BigDecimal.ZERO))
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
}
