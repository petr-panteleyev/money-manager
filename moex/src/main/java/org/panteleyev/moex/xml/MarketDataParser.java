/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.panteleyev.commons.xml.XMLEventReaderWrapper;
import org.panteleyev.moex.model.MoexMarketData;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public class MarketDataParser {
    private static final QName DATA = new QName("data");
    private static final QName ROW = new QName("row");

    private static final QName ATTR_ID = new QName("id");
    private static final QName ATTR_ACCRUEDINT = new QName("ACCRUEDINT");
    private static final QName ATTR_COUPONPERIOD = new QName("COUPONPERIOD");
    private static final QName ATTR_SECID = new QName("SECID");
    private static final QName ATTR_BOARDID = new QName("BOARDID");
    private static final QName ATTR_OPEN = new QName("OPEN");
    private static final QName ATTR_LOW = new QName("LOW");
    private static final QName ATTR_HIGH = new QName("HIGH");
    private static final QName ATTR_LAST = new QName("LAST");
    private static final QName ATTR_MARKETPRICE = new QName("MARKETPRICE");
    private static final QName ATTR_MARKETPRICETODAY = new QName("MARKETPRICETODAY");

    public MarketDataParser() {
    }

    public Optional<MoexMarketData> parseMarketData(InputStream inputStream) {
        try (var reader = XMLEventReaderWrapper.newInstance(inputStream)) {
            var builder = new MoexMarketData.Builder();

            while (reader.hasNext()) {
                var event = reader.nextEvent();

                event.ifStartElement(DATA, e -> Objects.equals(e.getAttributeValue(ATTR_ID, ""), "securities"), _ -> {
                    while (reader.hasNext()) {
                        var securitiesEvent = reader.nextEvent();
                        if (securitiesEvent.isEndElement(DATA)) {
                            break;
                        }

                        securitiesEvent.ifStartElement(ROW, row -> {
                            builder.accruedInterest(row.getAttributeValue(ATTR_ACCRUEDINT, BigDecimal.class).orElse(null))
                                    .couponPeriod(row.getAttributeValue(ATTR_COUPONPERIOD, Integer.class).orElse(null));
                        });
                    }
                });

                event.ifStartElement(DATA, e -> Objects.equals(e.getAttributeValue(ATTR_ID, ""), "marketdata"), _ -> {
                    while (reader.hasNext()) {
                        var marketDataEvent = reader.nextEvent();
                        if (marketDataEvent.isEndElement(DATA)) {
                            break;
                        }

                        marketDataEvent.ifStartElement(ROW, row -> {
                            builder.secId(row.getAttributeValue(ATTR_SECID).orElseThrow())
                                    .boardId(row.getAttributeValue(ATTR_BOARDID).orElseThrow())
                                    .open(row.getAttributeValue(ATTR_OPEN, BigDecimal.class).orElse(null))
                                    .low(row.getAttributeValue(ATTR_LOW, BigDecimal.class).orElse(null))
                                    .high(row.getAttributeValue(ATTR_HIGH, BigDecimal.class).orElse(null))
                                    .last(row.getAttributeValue(ATTR_LAST, BigDecimal.class).orElse(null))
                                    .marketPrice(row.getAttributeValue(ATTR_MARKETPRICE, BigDecimal.class).orElse(null))
                                    .marketPriceToday(row.getAttributeValue(ATTR_MARKETPRICETODAY, BigDecimal.class)
                                            .orElse(null));
                        });
                    }
                });
            }

            return builder.getSecId() == null ? Optional.empty() : Optional.of(builder.build());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}