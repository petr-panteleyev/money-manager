/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import org.panteleyev.money.client.graphql.GQLRequestBody;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.net.HttpURLConnection.HTTP_OK;

class GraphQLClient {
    private final URI url;
    private final HttpClient httpClient;
    private final JsonHandler<Object> jsonHandler;

    public GraphQLClient(URI url, HttpClient httpClient) {
        this.url = url;
        this.httpClient = httpClient;
        this.jsonHandler = new JsonHandler<>(Object.class);
    }

    <T> T query(String query, Class<T> responseClass, Map<String, Object> variables) {
        var body = new GQLRequestBody(query, variables);

        var request = HttpRequest.newBuilder(url)
                .POST(HttpRequest.BodyPublishers.ofByteArray(jsonHandler.convert(body)))
                .setHeader("Content-Type", "application/json")
                .build();

        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofInputStream());
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("Cannot execute GraphQL query");
            }
            try (var inputStream = response.body()) {
                return new JsonHandler<>(responseClass).jsonToObject(inputStream);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
