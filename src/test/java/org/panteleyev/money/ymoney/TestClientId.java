package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.testng.Assert;
import org.testng.annotations.Test;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.UUID;

public class TestClientId {
    private static final String TEST_PROPERTIES = "org.panteleyev.money.ymoney.ymoney";
    private static final String TEST_CLIENT_ID = "1234567890ABCDEFGH";
    private static final String TEST_REDIRECT_URI = "http://www.test.com";

    @Test
    public void testNewInstance() {
        for (int i = 0; i < 100; i++) {
            String clientId = UUID.randomUUID().toString();
            String redirectUri = UUID.randomUUID().toString();
            String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
            ClientId id = new ClientId(clientId, redirectUri, encodedRedirectUri);
            Assert.assertEquals(id.clientId(), clientId);
            Assert.assertEquals(id.redirectUri(), redirectUri);
        }
    }

    @Test
    public void testEquals() {
        var clientId = UUID.randomUUID().toString();
        var redirectUri = UUID.randomUUID().toString();
        String encodedRedirectUri = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);

        var id1 = new ClientId(clientId, redirectUri, encodedRedirectUri);
        var id2 = new ClientId(clientId, redirectUri, encodedRedirectUri);

        Assert.assertEquals(id1, id2);
        Assert.assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testLoad() {
        var id = ClientId.load(ResourceBundle.getBundle(TEST_PROPERTIES));
        Assert.assertEquals(id.clientId(), TEST_CLIENT_ID);
        Assert.assertEquals(id.redirectUri(), TEST_REDIRECT_URI);
    }
}
