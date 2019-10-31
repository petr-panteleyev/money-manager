/*
 * Copyright (c) 2019, Petr Panteleyev <petr@panteleyev.org>
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

package org.panteleyev.money.ymoney;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class AuthResponse {
    private final String code;
    private final String error;
    private final String errorDescription;

    public AuthResponse(String uri) {
        int index = uri.indexOf('?');
        if (index == -1) {
            throw new IllegalArgumentException("Invalid uri");
        }

        var parse = uri.substring(index + 1);

        String sToken = null;
        String sError = null;
        String sErrorDescription = null;

        var sections = parse.split("&");

        for (var section : sections) {
            var tokens = section.split("=");
            if ("code".equals(tokens[0])) {
                sToken = tokens[1];
                break;
            } else if ("error".equals(tokens[0])) {
                sError = tokens[1];
            } else if ("error_description".equals(tokens[0])) {
                sErrorDescription = URLDecoder.decode(tokens[1], StandardCharsets.UTF_8);
            }
        }

        code = sToken;
        error = sError;
        errorDescription = sErrorDescription;
    }

    public String getCode() {
        return code;
    }

    public String getError() {
        return error;
    }

    String getErrorDescription() {
        return errorDescription;
    }
}
