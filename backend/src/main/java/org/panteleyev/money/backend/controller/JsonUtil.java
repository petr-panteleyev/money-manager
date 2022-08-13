/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

interface JsonUtil {
    byte[] NEWLINE = "\n".getBytes(StandardCharsets.UTF_8);

    static void writeObjectToStream(OutputStream out, ObjectMapper mapper, Object object) {
        try {
            out.write(mapper.writeValueAsBytes(object));
            out.write(NEWLINE);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
