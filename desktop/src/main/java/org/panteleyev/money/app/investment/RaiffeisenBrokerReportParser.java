/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.investment.ExcelUtil.getCellValueAsString;

public class RaiffeisenBrokerReportParser {
    private static final String COMPLETE_DEALS = "Исполненные сделки";
    private static final String TABLE_END_ROW_CELL_VALUE = " Московская биржа. Итого, по всем сделкам с расчетами в рублях, в RUB";

    private static final int CELL_INDEX_DEAL_DATE = 2;
    private static final int CELL_INDEX_ACCOUNTING_DATE = 3;
    private static final int CELL_INDEX_DEAL_TIME = 5;
    private static final int CELL_INDEX_OPERATION_TYPE = 6;
    private static final int CELL_INDEX_SECURITY_AMOUNT = 7;
    private static final int CELL_INDEX_PRICE = 8;
    private static final int CELL_INDEX_DEAL_VOLUME = 10;
    private static final int CELL_INDEX_ACI = 11;
    private static final int CELL_INDEX_BROKER_FEE = 12;
    private static final int CELL_INDEX_EXCHANGE_FEE = 14;
    private static final int CELL_INDEX_CURRENCY_NAME = 15;
    private static final int CELL_INDEX_ISIN = 16;
    private static final int CELL_INDEX_DEAL_NUMBER = 17;

    public List<InvestmentDeal> parse(String accountName, InputStream inputStream) {
        var result = new ArrayList<InvestmentDeal>();

        try {
            var workbook = new XSSFWorkbook(inputStream);
            if (workbook.getNumberOfSheets() == 0) {
                return result;
            }

            var sheet = workbook.getSheetAt(0);

            var rowIterator = sheet.rowIterator();

            boolean dealsFound = false;

            // Ищем строку "Исполненные сделки" в столбце B
            while (rowIterator.hasNext()) {
                var row = rowIterator.next();
                var cellB = row.getCell(1);
                var cellBValue = getCellValue(cellB);

                if (Objects.equals(cellBValue.toString(), COMPLETE_DEALS)) {
                    dealsFound = true;
                    break;
                }
            }

            if (!dealsFound) {
                return result;
            }

            var accountUuid = cache().getAccounts().stream()
                    .filter(x -> Objects.equals(x.name(), accountName))
                    .map(Account::uuid)
                    .findAny()
                    .orElse(null);

            if (accountUuid == null) {
                // Cannot insert records w/o account
                return result;
            }

            while (rowIterator.hasNext()) {
                var row = rowIterator.next();

                var cellB = row.getCell(1);
                var cellBValue = getCellValue(cellB);

                if (Objects.equals(cellBValue.toString(), TABLE_END_ROW_CELL_VALUE)) {
                    return result;
                }

                if (getCellValue(row.getCell(CELL_INDEX_DEAL_DATE)) instanceof LocalDateTime dealDate) {
                    var columnValues = new ArrayList<>();
                    for (var cell : row) {
                        columnValues.add(getCellValue(cell));
                    }

                    var dealTime = LocalTime.parse(columnValues.get(CELL_INDEX_DEAL_TIME).toString());

                    var currencyName = columnValues.get(CELL_INDEX_CURRENCY_NAME);
                    var currencyUuid = cache().getCurrencies().stream()
                            .filter(x -> Objects.equals(x.symbol(), currencyName))
                            .map(Currency::uuid)
                            .findAny()
                            .orElse(null);

                    var isin = columnValues.get(CELL_INDEX_ISIN).toString();
                    var securityUuid = cache().getExchangeSecurities().stream()
                            .filter(x -> Objects.equals(x.isin(), isin))
                            .map(ExchangeSecurity::uuid)
                            .findAny()
                            .orElse(null);

                    var operationType = InvestmentOperationType.fromTitle(columnValues.get(CELL_INDEX_OPERATION_TYPE).toString());
                    var exchangeFee = (BigDecimal) columnValues.get(CELL_INDEX_EXCHANGE_FEE);
                    var brokerFee = (BigDecimal) columnValues.get(CELL_INDEX_BROKER_FEE);
                    var dealVolume = (BigDecimal) columnValues.get(CELL_INDEX_DEAL_VOLUME);

                    var amount = operationType == InvestmentOperationType.PURCHASE ?
                            dealVolume.add(exchangeFee).add(brokerFee)
                            : dealVolume.subtract(exchangeFee).subtract(brokerFee);

                    var investment = new InvestmentDeal.Builder()
                            .accountUuid(accountUuid)
                            .securityUuid(securityUuid)
                            .currencyUuid(currencyUuid)
                            .dealNumber(getCellValueAsString(row.getCell(CELL_INDEX_DEAL_NUMBER)))
                            .dealDate(dealDate.toLocalDate().atTime(dealTime))
                            .accountingDate((LocalDateTime) columnValues.get(CELL_INDEX_ACCOUNTING_DATE))
                            .marketType(InvestmentMarketType.STOCK_MARKET)
                            .operationType(operationType)
                            .securityAmount(((BigDecimal) columnValues.get(CELL_INDEX_SECURITY_AMOUNT)).intValue())
                            .price((BigDecimal) columnValues.get(CELL_INDEX_PRICE))
                            .aci((BigDecimal) columnValues.get(CELL_INDEX_ACI))
                            .dealVolume(dealVolume)
                            .rate(BigDecimal.ONE)
                            .exchangeFee(exchangeFee)
                            .brokerFee(brokerFee)
                            .amount(amount)
                            .dealType(InvestmentDealType.NORMAL)
                            .build();
                    result.add(investment);
                }
            }

            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getRichStringCellValue().getString();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                } else {
                    yield BigDecimal.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }
}
