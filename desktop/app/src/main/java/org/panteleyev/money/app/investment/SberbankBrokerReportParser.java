/*
 Copyright © 2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.investment;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Sheet;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.panteleyev.money.app.GlobalContext.cache;
import static org.panteleyev.money.app.investment.ExcelUtil.getCellValueAsString;

public class SberbankBrokerReportParser {
    private final static String DEALS_SHEET_NAME = "Сделки";

    private static final int CELL_INDEX_ACCOUNT = 0;
    private static final int CELL_INDEX_DEAL_NUMBER = 1;
    private static final int CELL_INDEX_DEAL_DATE = 2;
    private static final int CELL_INDEX_ACCOUNTING_DATE = 3;
    private static final int CELL_INDEX_SECURITY_ID = 4;
    private static final int CELL_INDEX_MARKET_TYPE_TYPE = 6;
    private static final int CELL_INDEX_OPERATION_TYPE = 7;
    private static final int CELL_INDEX_SECURITY_AMOUNT = 8;
    private static final int CELL_INDEX_PRICE = 9;
    private static final int CELL_INDEX_ACI = 10;
    private static final int CELL_INDEX_DEAL_VOLUME = 11;
    private static final int CELL_INDEX_CURRENCY_NAME = 12;
    private static final int CELL_INDEX_RATE = 13;
    private static final int CELL_INDEX_EXCHANGE_FEE = 14;
    private static final int CELL_INDEX_BROKER_FEE = 15;
    private static final int CELL_INDEX_AMOUNT = 16;
    private static final int CELL_INDEX_DEAL_TYPE = 17;

    public List<InvestmentDeal> parse(InputStream inputStream) {
        var result = new ArrayList<InvestmentDeal>();

        try {
            var workbook = new XSSFWorkbook(inputStream);
            if (workbook.getNumberOfSheets() == 0) {
                return result;
            }

            // Find deals sheet
            Sheet deals = null;
            for (var sheet : workbook) {
                if (DEALS_SHEET_NAME.equals(sheet.getSheetName())) {
                    deals = sheet;
                    break;
                }
            }
            if (deals == null) {
                return result;
            }

            result.ensureCapacity(deals.getLastRowNum());

            for (var row : deals) {
                // Skip first row
                if (row.getRowNum() == 0) {
                    continue;
                }

                var columnValues = new ArrayList<>();

                for (var cell : row) {
                    columnValues.add(getCellValue(cell));
                }

                var accountName = getCellValueAsString(row.getCell(CELL_INDEX_ACCOUNT));
                var accountUuid = cache().getAccounts().stream()
                        .filter(x -> Objects.equals(x.name(), accountName))
                        .map(Account::uuid)
                        .findAny()
                        .orElse(null);

                if (accountUuid == null) {
                    // Cannot insert record w/o account
                    continue;
                }

                var currencyName = columnValues.get(CELL_INDEX_CURRENCY_NAME);
                var currencyUuid = cache().getCurrencies().stream()
                        .filter(x -> Objects.equals(x.symbol(), currencyName))
                        .map(Currency::uuid)
                        .findAny()
                        .orElse(null);

                var securityId = columnValues.get(CELL_INDEX_SECURITY_ID);
                var securityUuid = cache().getExchangeSecurities().stream()
                        .filter(x -> Objects.equals(x.secId(), securityId))
                        .map(ExchangeSecurity::uuid)
                        .findAny()
                        .orElse(null);

                var investment = new InvestmentDeal.Builder()
                        .accountUuid(accountUuid)
                        .securityUuid(securityUuid)
                        .currencyUuid(currencyUuid)
                        .dealNumber(getCellValueAsString(row.getCell(CELL_INDEX_DEAL_NUMBER)))
                        .dealDate((LocalDateTime) columnValues.get(CELL_INDEX_DEAL_DATE))
                        .accountingDate((LocalDateTime) columnValues.get(CELL_INDEX_ACCOUNTING_DATE))
                        .marketType(InvestmentMarketType.fromTitle(columnValues.get(CELL_INDEX_MARKET_TYPE_TYPE).toString()))
                        .operationType(InvestmentOperationType.fromTitle(columnValues.get(CELL_INDEX_OPERATION_TYPE).toString()))
                        .securityAmount(((BigDecimal) columnValues.get(CELL_INDEX_SECURITY_AMOUNT)).intValue())
                        .price((BigDecimal) columnValues.get(CELL_INDEX_PRICE))
                        .aci((BigDecimal) columnValues.get(CELL_INDEX_ACI))
                        .dealVolume((BigDecimal) columnValues.get(CELL_INDEX_DEAL_VOLUME))
                        .rate((BigDecimal) columnValues.get(CELL_INDEX_RATE))
                        .exchangeFee((BigDecimal) columnValues.get(CELL_INDEX_EXCHANGE_FEE))
                        .brokerFee((BigDecimal) columnValues.get(CELL_INDEX_BROKER_FEE))
                        .amount((BigDecimal)columnValues.get(CELL_INDEX_AMOUNT))
                        .dealType(InvestmentDealType.fromTitle(columnValues.get(CELL_INDEX_DEAL_TYPE).toString()))
                        .build();
                result.add(investment);
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
