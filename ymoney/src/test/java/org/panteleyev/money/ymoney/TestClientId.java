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
