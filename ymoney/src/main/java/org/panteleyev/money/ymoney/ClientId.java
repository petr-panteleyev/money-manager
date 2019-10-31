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
