/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class JsonHandler<T> {
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .addModule(new Jdk8Module())
            .configure(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS, false)
            .build();

    private final Class<T> objectClass;

    public JsonHandler(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    public T parseObject(InputStream inputStream) {
        try (var jsonParser = objectMapper.createParser(inputStream)) {
            if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Cannot parse server response");
            }

            var tree = objectMapper.readTree(jsonParser);
            return objectMapper.convertValue(tree, objectClass);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public List<T> parseArray(InputStream inputStream) {
        try (var parser = objectMapper.createParser(inputStream)) {
            var result = new ArrayList<T>();

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Stream is not opened properly");
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                var tree = objectMapper.readTree(parser);
                var object = objectMapper.convertValue(tree, objectClass);
                result.add(object);
            }

            if (parser.currentToken() != JsonToken.END_ARRAY) {
                throw new IOException("Stream is not closed properly");
            }

            return result;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void parseStream(InputStream inputStream, Consumer<List<T>> listConsumer, int chunkSize) {
        try (var parser = objectMapper.createParser(inputStream)) {

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException("Stream is not opened properly");
            }

            var list = new ArrayList<T>(chunkSize);
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                var tree = objectMapper.readTree(parser);
                var object = objectMapper.convertValue(tree, objectClass);
                list.add(object);
                if (list.size() == chunkSize) {
                    listConsumer.accept(list);
                    list.clear();
                }
            }

            if (parser.currentToken() != JsonToken.END_ARRAY) {
                throw new IOException("Stream is not closed properly");
            }

            if (!list.isEmpty()) {
                listConsumer.accept(list);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public byte[] convert(T record) {
        try {
            return objectMapper.writeValueAsBytes(record);
        } catch (JsonProcessingException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public Map<String, Object> jsonToMap(InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream, HashMap.class);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public T jsonToObject(InputStream inputStream) {
        try {
            return objectMapper.readValue(inputStream, objectClass);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
