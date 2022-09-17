/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

final class JsonUtil {
    static <T> void writeStreamAsJsonArray(
            ObjectMapper mapper,
            Stream<T> stream,
            OutputStream outputStream
    ) throws IOException {
        try (var generator = mapper.getFactory().createGenerator(outputStream)) {
            generator.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT)
                    .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

            generator.writeStartArray();
            stream.forEach(object -> {
                try {
                    mapper.writeValue(generator, object);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
            generator.writeEndArray();
        }
    }

    private JsonUtil() {
    }
}
