/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.panteleyev.fx.PredicateProperty;
import org.panteleyev.money.app.BaseController;
import org.panteleyev.money.app.filters.ExchangeSecuritySelectionBox;
import org.panteleyev.money.app.filters.InvestmentDealFilterBox;
import org.panteleyev.money.model.investment.InvestmentDeal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Objects;

import static org.controlsfx.control.action.ActionUtils.createMenuItem;
import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.money.app.Constants.FILTER_RAIF_ONLINE_BROKER_DEALS;
import static org.panteleyev.money.app.Constants.FILTER_SBER_ONLINE_BROKER_DEALS;
import static org.panteleyev.money.app.Constants.FILTER_SBER_ONLINE_BROKER_DEALS_HTML;
import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.GlobalContext.dao;
import static org.panteleyev.money.app.GlobalContext.settings;
import static org.panteleyev.money.app.Styles.BIG_INSETS;
import static org.panteleyev.money.app.Styles.BIG_SPACING;

public class InvestmentDealsWindowController extends BaseController {
    private final PredicateProperty<InvestmentDeal> filterProperty;

    private final ExchangeSecuritySelectionBox securitySelectionBox = new ExchangeSecuritySelectionBox();
    private final InvestmentDealFilterBox dealFilterBox = new InvestmentDealFilterBox();

    public InvestmentDealsWindowController() {
        var filteredList = cache().getInvestmentDeals().filtered(dealFilterBox.getDealFilter());
        var tableView = new InvestmentDealsTableView(filteredList);

        filterProperty = PredicateProperty.and(List.of(
                securitySelectionBox.investmentDealPredicateProperty(),
                dealFilterBox.predicateProperty()
        ));
        filterProperty.addListener((_, _, _) -> tableView.setTransactionFilter(filterProperty.get()));
        dealFilterBox.setDealFilter(InvestmentDealPredicate.LAST_QUARTER);

        dealFilterBox.setFilterYears(cache().getInvestmentDeals());

        var filterBox = hBox(
                List.of(
                        label("Ценные бумаги:"),
                        securitySelectionBox,
                        label("Даты:"),
                        dealFilterBox
                ), b -> {
                    b.setAlignment(Pos.CENTER_LEFT);
                    b.setSpacing(BIG_SPACING);
                    HBox.setMargin(securitySelectionBox, new Insets(0, BIG_SPACING, 0, 0));
                }
        );

        var mainPane = new BorderPane(
                tableView, filterBox, null, null, null
        );
        BorderPane.setMargin(filterBox, BIG_INSETS);

        var self = new BorderPane(
                mainPane,
                createMenu(), null, null, null
        );

        setupWindow(self);
        settings().loadStageDimensions(this);
    }

    @Override
    public String getTitle() {
        return "Инвестиции";
    }

    private MenuBar createMenu() {
        return menuBar(
                menu("Файл",
                        menuItem("Загрузить сделки", _ -> onLoadInvestmentDeals()),
                        new SeparatorMenuItem(),
                        createMenuItem(ACTION_CLOSE)
                ),
                createWindowMenu(),
                createHelpMenu()
        );
    }

    private void onLoadInvestmentDeals() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Сделки");
        fileChooser.getExtensionFilters().addAll(
//                FILTER_SBER_ONLINE_BROKER_DEALS,
                FILTER_SBER_ONLINE_BROKER_DEALS_HTML,
                FILTER_RAIF_ONLINE_BROKER_DEALS);

        var selectedFiles = fileChooser.showOpenMultipleDialog(this.getStage());
        if (selectedFiles == null || selectedFiles.isEmpty()) {
            return;
        }

        for (var selected : selectedFiles) {
            try (var inputStream = new FileInputStream(selected)) {
                var filter = fileChooser.getSelectedExtensionFilter();

                List<InvestmentDeal> investments;

                if (Objects.equals(filter, FILTER_SBER_ONLINE_BROKER_DEALS)) {
                    investments = new SberbankBrokerReportParser().parse(inputStream);
                } else if (Objects.equals(filter, FILTER_SBER_ONLINE_BROKER_DEALS_HTML)) {
                    investments = new SberbankBrokerHtmlReportParser().parse(inputStream);
                } else if (Objects.equals(filter, FILTER_RAIF_ONLINE_BROKER_DEALS)) {
                    var fileName = selected.getName();
                    var underscoreIndex = fileName.indexOf('_');

                    var accountName = "";
                    if (underscoreIndex != -1) {
                        accountName = fileName.substring(0, underscoreIndex);
                    }

                    investments = new RaiffeisenBrokerReportParser().parse(accountName, inputStream);
                } else {
                    investments = List.of();
                }
                dao().insertInvestments(investments);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    private void resetFilter() {
        dealFilterBox.setDealFilter(InvestmentDealPredicate.LAST_QUARTER);
    }
}
