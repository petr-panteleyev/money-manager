package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public record AuthResponse(String code, String error, String errorDescription) {
    public static AuthResponse of(String uri) {
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

        return new AuthResponse(sToken, sError, sErrorDescription);
    }
}
