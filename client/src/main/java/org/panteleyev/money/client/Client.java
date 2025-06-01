/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import org.panteleyev.money.model.MoneyRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static java.net.HttpURLConnection.HTTP_ACCEPTED;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;

class Client<T extends MoneyRecord> {
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    private final URI url;
    private final HttpClient httpClient;

    private final JsonHandler<T> jsonHandler;
    private final int streamingChunkSize;

    public Client(URI url, HttpClient httpClient, Class<T> objectClass, int streamingChunkSize) {
        this.url = url;
        this.httpClient = httpClient;
        this.jsonHandler = new JsonHandler<>(objectClass);
        this.streamingChunkSize = streamingChunkSize;
    }

    public List<T> getAll() {
        var request = HttpRequest.newBuilder(url)
                .GET()
                .setHeader("Accept", APPLICATION_JSON)
                .build();

        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofInputStream());
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("Cannot get " + url);
            }

            try (var inputStream = response.body()) {
                return jsonHandler.parseArray(inputStream);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void getAllAsStream(Consumer<List<T>> listConsumer) {
        var request = HttpRequest.newBuilder(URI.create(url.toString() + "/stream"))
                .GET()
                .setHeader("Accept", APPLICATION_OCTET_STREAM)
                .build();

        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofInputStream());
            if (response.statusCode() != HTTP_ACCEPTED) {
                throw new IOException("Cannot retrieve stream");
            }

            try (var inputStream = response.body()) {
                jsonHandler.parseStream(inputStream, listConsumer, streamingChunkSize);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Optional<T> get(UUID uuid) {
        var request = HttpRequest.newBuilder(URI.create(url.toString() + "/" + uuid.toString()))
                .GET()
                .setHeader("Accept", APPLICATION_JSON)
                .build();

        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofInputStream());
            if (response.statusCode() == HTTP_NOT_FOUND) {
                return Optional.empty();
            }
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("Cannot get " + url);
            }

            try (var inputStream = response.body()) {
                return Optional.of(jsonHandler.parseObject(inputStream));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public T put(T record) {
        var request = HttpRequest.newBuilder(URI.create(url.toString() + "/" + record.uuid().toString()))
                .PUT(HttpRequest.BodyPublishers.ofByteArray(jsonHandler.convert(record)))
                .setHeader("Content-Type", APPLICATION_JSON)
                .setHeader("Accept", APPLICATION_JSON)
                .build();

        try {
            var response = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofInputStream());
            if (response.statusCode() != HTTP_OK) {
                throw new IOException("Cannot put record to " + url);
            }

            try (var inputStream = response.body()) {
                return jsonHandler.parseObject(inputStream);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
