/*
 * Copyright (c) 2018, Petr Panteleyev <petr@panteleyev.org>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.panteleyev.money;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;

public interface XMLUtils {
    static void writeXmlHeader(Writer w) throws IOException {
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    }

    static void writeTag(Writer w, String tag, String value) throws IOException {
        w.write("<" + tag + ">"
                + (value == null ? "" : value)
                + "</" + tag + ">");
    }

    static void writeTag(Writer w, String tag, int value) throws IOException {
        writeTag(w, tag, Integer.toString(value));
    }

    static void writeTag(Writer w, String tag, long value) throws IOException {
        writeTag(w, tag, Long.toString(value));
    }

    static void writeTag(Writer w, String tag, boolean value) throws IOException {
        writeTag(w, tag, Boolean.toString(value));
    }

    static void writeTag(Writer w, String tag, BigDecimal value) throws IOException {
        writeTag(w, tag, value.toString());
    }

    static void openTag(Writer w, String tag) throws IOException {
        w.write("<" + tag + ">");
    }

    static void openTag(Writer w, String tag, int id) throws IOException {
        w.write("<" + tag + " id=\"" + Integer.toString(id) + "\">");
    }

    static void closeTag(Writer w, String tag) throws IOException {
        w.write("</" + tag + ">");
    }
}
