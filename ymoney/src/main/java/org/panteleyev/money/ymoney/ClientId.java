package org.panteleyev.money.ymoney;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * This class holds client ID for Yandex Money.
 */
public final class ClientId {
    private final String clientId;
    private final String redirectUri;
    private final String encodedRedirectUri;

    /**
     * Creates new instance with parameters.
     *
     * @param clientId    client ID
     * @param redirectUri redirect URI
     */
    ClientId(String clientId, String redirectUri) {
        Objects.requireNonNull(clientId, redirectUri);
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
    }

    /**
     * Returns client ID.
     *
     * @return client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Returns redirect URI.
     *
     * @return redirect URI
     */
    public String getRedirectUri() {
        return redirectUri;
    }

    /**
     * Returns encoded redirect URI.
     *
     * @return encoded redirect uri
     */
    public String getEncodedRedirectUri() {
        return encodedRedirectUri;
    }

    /**
     * This method loads client ID from specified properties file.
     *
     * @param propertyFile property file
     * @return new instance
     */
    public static ClientId load(String propertyFile) {
        ResourceBundle rb = ResourceBundle.getBundle(propertyFile);
        return new ClientId(rb.getString("client_id"), rb.getString("redirect_uri"));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClientId
                && Objects.equals(this.clientId, ((ClientId) obj).clientId)
                && Objects.equals(this.redirectUri, ((ClientId) obj).redirectUri)
                && Objects.equals(this.encodedRedirectUri, ((ClientId) obj).encodedRedirectUri);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, redirectUri, encodedRedirectUri);
    }
}
