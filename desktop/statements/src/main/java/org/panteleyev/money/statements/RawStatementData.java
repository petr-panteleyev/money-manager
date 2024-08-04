/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RawStatementData {
    private final String content;

    public RawStatementData(File file) {
        try {
            var bytes = Files.readAllBytes(file.toPath());
            content = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public RawStatementData(byte[] bytes) {
        this.content = new String(bytes, StandardCharsets.UTF_8);
    }

    public String getContent() {
        return content;
    }
}
