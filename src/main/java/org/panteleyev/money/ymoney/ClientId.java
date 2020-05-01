package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * This class holds client ID for Yandex Money.
 */
public record ClientId(String clientId, String redirectUri, String encodedRedirectUri) {
    /**
     * This method loads client ID from specified property file.
     *
     * @param bundle property file
     * @return new instance
     */
    public static ClientId load(ResourceBundle bundle) {
        var clientId = bundle.getString("client_id");
        var redirectUri = bundle.getString("redirect_uri");
        var encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return new ClientId(clientId, redirectUri, encodedRedirectUri);
    }
}
