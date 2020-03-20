package org.panteleyev.money.ymoney;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

/**
 * This class holds client ID for Yandex Money.
 */
public record ClientId(String clientId, String redirectUri, String encodedRedirectUri) {
    /**
     * This method loads client ID from specified properties file.
     *
     * @param propertyFile property file
     * @return new instance
     */
    public static ClientId load(String propertyFile) {
        ResourceBundle rb = ResourceBundle.getBundle(propertyFile);
        var clientId = rb.getString("client_id");
        var redirectUri = rb.getString("redirect_uri");
        var encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        return new ClientId(clientId, redirectUri, encodedRedirectUri);
    }
}
