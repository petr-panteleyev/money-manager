/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money.ymoney;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.panteleyev.money.Options;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementRecord;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class YandexMoneyClient {
    private static final String OAUTH_ENDPOINT = "https://money.yandex.ru/oauth/authorize";
    private static final String TOKEN_ENDPOINT = "https://money.yandex.ru/oauth/token";
    private static final String API_ENDPOINT = "https://money.yandex.ru/api";

    private static final Logger LOGGER = Logger.getLogger(YandexMoneyClient.class.getName());

    private static final String YM_PROPS = "org.panteleyev.money.ymoney";

    private final ClientId clientId;

    private String token;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpRequest.Builder tokenRequestBuilder;
    private final HttpRequest.Builder accountInfoRequestBuilder;
    private final HttpRequest.Builder historyRequestBuilder;

    public YandexMoneyClient() {
        clientId = ClientId.load(YM_PROPS);
        token = Options.getYandexMoneyToken();

        try {
            tokenRequestBuilder = HttpRequest.newBuilder(new URI(TOKEN_ENDPOINT));
            historyRequestBuilder = HttpRequest.newBuilder(new URI(API_ENDPOINT + "/operation-history"));
            accountInfoRequestBuilder = HttpRequest.newBuilder(new URI(API_ENDPOINT + "/account-info"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void authorize() {
        var scope = URLEncoder.encode("operation-details account-info operation-history", StandardCharsets.UTF_8);

        var authUrl = OAUTH_ENDPOINT
                + "?client_id=" + clientId.getClientId()
                + "&scope=" + scope
                + "&response_type=code"
                + "&redirect_uri=" + clientId.getEncodedRedirectUri();

        var authDialog = new WebAuthDialog(authUrl, clientId.getRedirectUri());
        authDialog.responseUriProperty().addListener((x, y, redirectUri) -> onAuthComplete(redirectUri));

        var stage = authDialog.getStage();
        stage.show();
        stage.toFront();
    }

    public Statement load(int limit, LocalDate from, LocalDate to) {
        BigDecimal balance = accountInfoRequest().map(AccountInfo::getBalance).orElse(BigDecimal.ZERO);

        try {
            var body = "records=" +
                    Integer.toString(limit) +
                    "&details=true" +
                    "&from=" + DateTimeFormatter.ISO_DATE.format(from) +
                    "&till=" + DateTimeFormatter.ISO_DATE.format(to);

            var httpRequest = historyRequestBuilder
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if (status != 200) {
                throw new RuntimeException("Invalid history");
            }

            var element = (JsonObject) new JsonParser().parse(httpResponse.body());
            var operations = element.get("operations").getAsJsonArray();

            var statementRecords = new ArrayList<StatementRecord>();

            for (var opn : operations) {
                var operation = new Operation((JsonObject) opn);

                var title = operation.getDetails() == null ?
                        operation.getTitle() : operation.getDetails();

                var record = new StatementRecord(
                        operation.getDate(), operation.getDate(),
                        title,
                        "", "", "",
                        "RUB",
                        operation.getAmount().toString(),
                        "RUB",
                        operation.getAmount().toString()
                );

                statementRecords.add(record);
            }

            return new Statement(Statement.StatementType.YANDEX_MONEY, statementRecords, balance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void onAuthComplete(String redirectUri) {
        var response = new AuthResponse(redirectUri);
        if (response.getError() == null) {
            try {
                var body = "code=" + response.getCode()
                        + "&client_id=" + clientId.getClientId()
                        + "&grant_type=authorization_code"
                        + "&redirect_uri=" + clientId.getEncodedRedirectUri();

                var httpRequest = tokenRequestBuilder
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                int status = httpResponse.statusCode();
                if (status != 200) {
                    throw new RuntimeException("Invalid response");
                }

                var element = (JsonObject) new JsonParser().parse(httpResponse.body());
                var error = element.get("error");
                if (error == null) {
                    var tokenElement = element.get("access_token");
                    if (tokenElement != null) {
                        token = tokenElement.getAsString();
                        Options.setYandexMoneyToken(token);
                    }
                } else {
                    LOGGER.log(Level.INFO, "Yandex Money token error: " + error);
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } else {
            LOGGER.log(Level.INFO, "Yandex Money authorization error: " + response.getError());
        }
    }

    private Optional<AccountInfo> accountInfoRequest() {
        if (token == null) {
            return Optional.empty();
        }

        try {
            var request = accountInfoRequestBuilder
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if (status != 200) {
                return Optional.empty();
            }
            var accountObject = (JsonObject) new JsonParser().parse(httpResponse.body());
            return Optional.of(new AccountInfo(accountObject));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
