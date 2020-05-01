package org.panteleyev.money.app;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.panteleyev.money.statements.Statement;
import org.panteleyev.money.statements.StatementRecord;
import org.panteleyev.money.ymoney.AccountInfo;
import org.panteleyev.money.ymoney.AuthResponse;
import org.panteleyev.money.ymoney.ClientId;
import org.panteleyev.money.ymoney.Operation;
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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

class YandexMoneyClient {
    private static final String OAUTH_ENDPOINT = "https://money.yandex.ru/oauth/authorize";
    private static final String TOKEN_ENDPOINT = "https://money.yandex.ru/oauth/token";
    private static final String API_ENDPOINT = "https://money.yandex.ru/api";

    private static final Logger LOGGER = Logger.getLogger(YandexMoneyClient.class.getName());

    private static final String YM_PROPS = "org.panteleyev.money.app.ymoney";

    private final ClientId clientId;

    private String token;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    YandexMoneyClient(String token) {
        clientId = ClientId.load(ResourceBundle.getBundle(YM_PROPS));
        this.token = token;
    }

    void authorize() {
        var scope = URLEncoder.encode("operation-details account-info operation-history", StandardCharsets.UTF_8);

        var authUrl = OAUTH_ENDPOINT
            + "?client_id=" + clientId.clientId()
            + "&scope=" + scope
            + "&response_type=code"
            + "&redirect_uri=" + clientId.encodedRedirectUri();

        var authDialog = new WebAuthDialog(authUrl, clientId.redirectUri());
        authDialog.responseUriProperty().addListener((x, y, redirectUri) -> onAuthComplete(redirectUri));

        var stage = authDialog.getStage();
        stage.show();
        stage.toFront();
    }

    Statement load(int limit, LocalDate from, LocalDate to) {
        var accountInfo = accountInfoRequest();
        var balance = accountInfo.map(AccountInfo::balance).orElse(BigDecimal.ZERO);
        var accountNumber = accountInfo.map(AccountInfo::id).orElse("");

        try {
            var body = "records=" +
                limit +
                "&details=true" +
                "&from=" + DateTimeFormatter.ISO_DATE.format(from) +
                "&till=" + DateTimeFormatter.ISO_DATE.format(to);

            var builder = HttpRequest.newBuilder(new URI(API_ENDPOINT + "/operation-history"));
            var httpRequest = builder
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if (status != 200) {
                throw new RuntimeException("Invalid history");
            }

            var element = (JsonObject) JsonParser.parseString(httpResponse.body());
            var operations = element.get("operations").getAsJsonArray();

            var statementRecords = new ArrayList<StatementRecord>();

            for (var opn : operations) {
                var operation = Operation.of((JsonObject) opn);

                var title = operation.details() == null ?
                    operation.title() : operation.details();

                var record = new StatementRecord(
                    operation.date(), operation.date(),
                    title,
                    "", "", "",
                    "RUB",
                    operation.amount().toString(),
                    "RUB",
                    operation.amount().toString()
                );

                statementRecords.add(record);
            }

            return new Statement(Statement.StatementType.YANDEX_MONEY, accountNumber, statementRecords, balance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void onAuthComplete(String redirectUri) {
        var response = AuthResponse.of(redirectUri);
        if (response.error() == null) {
            try {
                var body = "code=" + response.code()
                    + "&client_id=" + clientId.clientId()
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + clientId.encodedRedirectUri();

                var builder = HttpRequest.newBuilder(new URI(TOKEN_ENDPOINT));
                var httpRequest = builder
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
                var httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                int status = httpResponse.statusCode();
                if (status != 200) {
                    throw new RuntimeException("Invalid response");
                }

                var element = (JsonObject) JsonParser.parseString(httpResponse.body());
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
            LOGGER.log(Level.INFO, "Yandex Money authorization error: " + response.error());
        }
    }

    private Optional<AccountInfo> accountInfoRequest() {
        if (token == null) {
            return Optional.empty();
        }

        try {
            var builder = HttpRequest.newBuilder(new URI(API_ENDPOINT + "/account-info"));
            var request = builder
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
            var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = httpResponse.statusCode();
            if (status != 200) {
                return Optional.empty();
            }
            var accountObject = (JsonObject) JsonParser.parseString(httpResponse.body());
            return Optional.of(AccountInfo.of(accountObject));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
