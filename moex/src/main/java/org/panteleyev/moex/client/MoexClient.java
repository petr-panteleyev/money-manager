/*
 Copyright © 2023-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.client;

import org.panteleyev.moex.model.MoexEngine;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MoexClient {
    private static final String BASE_URL = "https://iss.moex.com/iss";
    private static final String ENGINES = "/engines";
    private static final String MARKETS = "/markets";
    private static final String BOARDS = "/boards";
    private static final String SECURITIES = "/securities";
    private static final String XML = ".xml";

    public HttpResponse<InputStream> getEngines(HttpClient httpClient) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + ENGINES + XML))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpResponse<InputStream> getMarkets(HttpClient httpClient, MoexEngine engine) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + ENGINES + "/" + engine.name() + MARKETS + XML))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpResponse<InputStream> getSecurity(HttpClient httpClient, String securityId) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL + SECURITIES + "/" + securityId + XML + "?iss.meta=off"))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public HttpResponse<InputStream> getMarketData(HttpClient httpClient, String securityId, String engine, String market, String board) {
        try {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(BASE_URL
                            + ENGINES + "/" + engine.toLowerCase()
                            + MARKETS + "/" + market.toLowerCase()
                            + BOARDS + "/" + board.toLowerCase()
                            + SECURITIES + "/" + securityId + XML + "?iss.meta=off"))
                    .GET()
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
