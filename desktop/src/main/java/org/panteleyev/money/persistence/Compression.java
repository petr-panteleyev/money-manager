/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

final class Compression {
    private Compression() {
    }

    static byte[] compress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }

        try (var byteArray = new ByteArrayOutputStream();
             var output = new DeflaterOutputStream(byteArray))
        {
            output.write(bytes);
            output.finish();
            return byteArray.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static byte[] decompress(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return bytes;
        }

        try (var input = new InflaterInputStream(new ByteArrayInputStream(bytes));
             var output = new ByteArrayOutputStream())
        {
            input.transferTo(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
