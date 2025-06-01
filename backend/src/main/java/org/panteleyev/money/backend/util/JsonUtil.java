/*
 Copyright © 2022-2025 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

public final class JsonUtil {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> void writeStreamAsJsonArray(
            ObjectMapper mapper,
            Stream<T> stream,
            OutputStream outputStream
    )
    {
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
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private JsonUtil() {
    }
}
