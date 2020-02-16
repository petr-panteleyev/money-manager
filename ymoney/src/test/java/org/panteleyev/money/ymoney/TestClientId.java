package org.panteleyev.money.ymoney;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.testng.Assert;
import org.testng.annotations.Test;
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
            ClientId id = new ClientId(clientId, redirectUri);
            Assert.assertEquals(id.getClientId(), clientId);
            Assert.assertEquals(id.getRedirectUri(), redirectUri);
        }
    }

    @Test(expectedExceptions = {NullPointerException.class})
    public void testNewInstanceNegative() {
        new ClientId(null, null);
    }

    @Test
    public void testEquals() {
        var clientId = UUID.randomUUID().toString();
        var redirectUri = UUID.randomUUID().toString();

        var id1 = new ClientId(clientId, redirectUri);
        var id2 = new ClientId(clientId, redirectUri);

        Assert.assertEquals(id1, id2);
        Assert.assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    public void testLoad() {
        var id = ClientId.load(TEST_PROPERTIES);
        Assert.assertEquals(id.getClientId(), TEST_CLIENT_ID);
        Assert.assertEquals(id.getRedirectUri(), TEST_REDIRECT_URI);
    }
}
