/*
 Copyright © 2022-2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.statements;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class RawStatementData {
    private final byte[] bytes;

    public RawStatementData(File file) {
        try {
            bytes = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public RawStatementData(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getContent() {
        return getContent(StandardCharsets.UTF_8);
    }

    public String getContent(Charset charset) {
        return new String(bytes, charset);
    }
}
