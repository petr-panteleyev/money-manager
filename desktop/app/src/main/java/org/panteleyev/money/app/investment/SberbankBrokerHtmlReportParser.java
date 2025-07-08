/*
 Copyright © 2024-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.Currency;
import org.panteleyev.money.model.exchange.ExchangeSecurity;
import org.panteleyev.money.model.investment.InvestmentDeal;
import org.panteleyev.money.model.investment.InvestmentDealType;
import org.panteleyev.money.model.investment.InvestmentMarketType;
import org.panteleyev.money.model.investment.InvestmentOperationType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.panteleyev.money.app.GlobalContext.cache;

public class SberbankBrokerHtmlReportParser {
    private static final String DEAL_COMPLETED = "И";

    private static final int DEAL_TABLE_COLUMN_COUNT = 16;

    private static final int CELL_INDEX_DEAL_DATE = 0;
    private static final int CELL_INDEX_ACCOUNTING_DATE = 1;
    private static final int CELL_INDEX_DEAL_TIME = 2;
    private static final int CELL_INDEX_SECURITY_ID = 4;
    private static final int CELL_INDEX_CURRENCY_NAME = 5;
    private static final int CELL_INDEX_OPERATION_TYPE = 6;
    private static final int CELL_INDEX_SECURITY_AMOUNT = 7;
    private static final int CELL_INDEX_PRICE = 8;
    private static final int CELL_INDEX_DEAL_VOLUME = 9;
    private static final int CELL_INDEX_ACI = 10;
    private static final int CELL_INDEX_BROKER_FEE = 11;
    private static final int CELL_INDEX_EXCHANGE_FEE = 12;
    private static final int CELL_INDEX_DEAL_NUMBER = 13;
    private static final int CELL_INDEX_DEAL_STATUS = 15;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public List<InvestmentDeal> parse(String fileName, InputStream inputStream) {
        var result = new ArrayList<InvestmentDeal>();

        try {
            var document = Jsoup.parse(inputStream, StandardCharsets.UTF_8.name(), "");

            var accountName = parseAccountName(fileName);
            var accountUuid = cache().getAccounts().stream()
                    .filter(x -> Objects.equals(x.name(), accountName))
                    .map(Account::uuid)
                    .findAny()
                    .orElse(null);
            if (accountUuid == null) {
                return result;
            }

            var table = document.getElementsByTag("table").stream()
                    .filter(this::checkForDealsTable)
                    .findFirst()
                    .orElse(null);
            if (table == null) {
                return result;
            }

            var rows = table.getElementsByTag("tr").stream()
                    .filter(e -> !e.hasClass("summary-row"))
                    .filter(e -> !e.hasClass("table-header"))
                    .filter(e -> !e.hasClass("rn"))
                    .map(e -> e.getElementsByTag("td"))
                    .filter(columns -> columns.size() == DEAL_TABLE_COLUMN_COUNT)
                    .toList();

            for (var row : rows) {
                var dealStatus = row.get(CELL_INDEX_DEAL_STATUS).text();
                if (!dealStatus.contains(DEAL_COMPLETED)) {
                    continue;
                }

                var currencyName = row.get(CELL_INDEX_CURRENCY_NAME).text();
                var currencyUuid = cache().getCurrencies().stream()
                        .filter(x -> Objects.equals(x.symbol(), currencyName))
                        .map(Currency::uuid)
                        .findAny()
                        .orElse(null);

                var securityId = row.get(CELL_INDEX_SECURITY_ID).text();
                var securityUuid = cache().getExchangeSecurities().stream()
                        .filter(x -> Objects.equals(x.secId(), securityId) || Objects.equals(x.isin(), securityId))
                        .map(ExchangeSecurity::uuid)
                        .findAny()
                        .orElse(null);

                var dealDate = LocalDate.parse(row.get(CELL_INDEX_DEAL_DATE).text(), DATE_FORMATTER);
                var dealTime = LocalTime.parse(row.get(CELL_INDEX_DEAL_TIME).text(), TIME_FORMATTER);

                var dealVolume = parseBigDecimal(row, CELL_INDEX_DEAL_VOLUME);
                var exchangeFee = parseBigDecimal(row, CELL_INDEX_EXCHANGE_FEE);
                var brokerFee = parseBigDecimal(row, CELL_INDEX_BROKER_FEE);
                var aci = parseBigDecimal(row, CELL_INDEX_ACI);
                var amount = dealVolume.add(exchangeFee).add(brokerFee).add(aci);

                var investment = new InvestmentDeal.Builder()
                        .accountUuid(accountUuid)
                        .securityUuid(securityUuid)
                        .currencyUuid(currencyUuid)
                        .dealNumber(row.get(CELL_INDEX_DEAL_NUMBER).text())
                        .dealDate(LocalDateTime.of(dealDate, dealTime))
                        .accountingDate(LocalDate.parse(row.get(CELL_INDEX_ACCOUNTING_DATE).text(),
                                DATE_FORMATTER).atStartOfDay())
                        .marketType(InvestmentMarketType.STOCK_MARKET) // TODO: parse from table
                        .operationType(InvestmentOperationType.fromTitle(row.get(CELL_INDEX_OPERATION_TYPE).text()))
                        .securityAmount(Integer.parseInt(
                                row.get(CELL_INDEX_SECURITY_AMOUNT).text()
                                        .replace(" ", "")
                        ))
                        .price(parseBigDecimal(row, CELL_INDEX_PRICE))
                        .aci(aci)
                        .dealVolume(dealVolume)
                        .rate(BigDecimal.ONE)   // TODO: где взять?
                        .exchangeFee(exchangeFee)
                        .brokerFee(brokerFee)
                        .amount(amount)
                        .dealType(InvestmentDealType.NORMAL)
                        .build();
                result.add(investment);
            }

            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String parseAccountName(Document document) {
        var titles = document.getElementsByTag("title");
        if (titles.isEmpty()) {
            throw new RuntimeException("Invalid document format");
        }
        var titleString = titles.getFirst().text().split(" ");
        return titleString[titleString.length - 1];
    }

    private String parseAccountName(String fileName) {
        var index = fileName.indexOf('_');
        return index != -1 ? fileName.substring(0, index) : "";
    }

    private boolean checkForDealsTable(Element table) {
        var header = table.getElementsByClass("table-header").first();
        if (header == null) {
            return false;
        }
        var columns = header.getElementsByTag("td");
        return columns.size() == DEAL_TABLE_COLUMN_COUNT;
    }

    private BigDecimal parseBigDecimal(Elements row, int index) {
        var stringValue = row.get(index).text().trim().replaceAll(" ", "");
        return new BigDecimal(stringValue);
    }
}
