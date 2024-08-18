/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex;

import org.panteleyev.moex.client.MoexClient;
import org.panteleyev.moex.model.MoexEngine;
import org.panteleyev.moex.model.MoexMarket;
import org.panteleyev.moex.model.MoexMarketData;
import org.panteleyev.moex.model.MoexSecurity;
import org.panteleyev.moex.xml.MarketDataParser;
import org.panteleyev.moex.xml.MoexParser;
import org.panteleyev.moex.xml.SecurityParser;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

public class Moex {
    private final MoexClient client = new MoexClient();
    private final MoexParser parser = new MoexParser();
    private final SecurityParser securityParser = new SecurityParser();
    private final MarketDataParser marketDataParser = new MarketDataParser();

    public List<MoexEngine> getEngines() {
        try (var httpClient = HttpClient.newHttpClient()) {
            var response = client.getEngines(httpClient);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed");
            }

            try (var inputStream = response.body()) {
                return parser.getEngines(inputStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public List<MoexMarket> getMarkets(MoexEngine engine) {
        try (var httpClient = HttpClient.newHttpClient()) {
            var response = client.getMarkets(httpClient, engine);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed");
            }

            try (var inputStream = response.body()) {
                return parser.getMarkets(inputStream, engine);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public Optional<MoexSecurity> getSecurity(String securityId) {
        try (var httpClient = HttpClient.newHttpClient()) {
            var response = client.getSecurity(httpClient, securityId);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed");
            }

            try (var inputStream = response.body()) {
                return securityParser.parseSecurity(inputStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public Optional<MoexMarketData> getMarketData(String securityId, String engine, String market, String board) {
        try (var httpClient = HttpClient.newHttpClient()) {
            var response = client.getMarketData(httpClient, securityId, engine, market, board);
            if (response.statusCode() != 200) {
                throw new RuntimeException("Request failed");
            }

            try (var inputStream = response.body()) {
                return marketDataParser.parseMarketData(inputStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
