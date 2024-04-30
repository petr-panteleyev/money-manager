/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.xml;

import org.panteleyev.moex.model.MoexMarketData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.xpath.XPathConstants;
import java.util.Optional;

import static org.panteleyev.moex.xml.ParserUtil.parseInt;
import static org.panteleyev.moex.xml.ParserUtil.parseNumber;

public class MarketDataParser extends BaseParser {
    private static final String SECURITY_XPATH = "/document/data[@id='securities']/rows/row[1]";
    private static final String ACCRUEDINT_XPATH = "/document/data[@id='securities']/rows/row[1]/@ACCRUEDINT";
    private static final String COUPONPERIOD_XPATH = "/document/data[@id='securities']/rows/row[1]/@COUPONPERIOD";

    private static final String MARKET_DATA_XPATH = "/document/data[@id='marketdata']/rows/row[1]";

    public MarketDataParser() {
    }

    public Optional<MoexMarketData> parse(Document document) throws Exception {
        var builder = new MoexMarketData.Builder();

        var securities = (Element) xPath().compile(SECURITY_XPATH).evaluate(document, XPathConstants.NODE);
        if (securities == null) {
            return Optional.empty();
        }

        builder.accruedInterest(parseNumber(securities.getAttribute("ACCRUEDINT")))
                .couponPeriod(parseInt(securities.getAttribute("COUPONPERIOD")));

        var marketData = (Element) xPath().compile(MARKET_DATA_XPATH).evaluate(document, XPathConstants.NODE);

        builder.secId(marketData.getAttribute("SECID"))
                .boardId(marketData.getAttribute("BOARDID"))
                .open(parseNumber(marketData.getAttribute("OPEN")))
                .low(parseNumber(marketData.getAttribute("LOW")))
                .high(parseNumber(marketData.getAttribute("HIGH")))
                .last(parseNumber(marketData.getAttribute("LAST")))
                .marketPrice(parseNumber(marketData.getAttribute("MARKETPRICE")))
                .marketPriceToday(parseNumber(marketData.getAttribute("MARKETPRICETODAY")))
        ;

        return Optional.of(builder.build());
    }
}