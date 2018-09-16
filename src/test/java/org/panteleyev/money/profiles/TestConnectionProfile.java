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

package org.panteleyev.money.profiles;

import org.testng.Assert;
import org.testng.annotations.Test;
import static org.panteleyev.money.BaseTestUtils.randomPort;
import static org.panteleyev.money.BaseTestUtils.randomString;

public class TestConnectionProfile extends ProfileTestBase {

    @Test
    public void testEquals() {
        var name = randomString();
        var databaseHost = randomString();
        int databasePort = randomPort();
        var databaseUser = randomString();
        var databasePassword = randomString();
        var schema = randomString();
        var encryptionKey = randomString();
        var sshSession = randomString();

        var p1 = new ConnectionProfile(
                name,
                databaseHost,
                databasePort,
                databaseUser,
                databasePassword,
                schema,
                encryptionKey,
                sshSession);

        var p2 = new ConnectionProfile(
                name,
                databaseHost,
                databasePort,
                databaseUser,
                databasePassword,
                schema,
                encryptionKey,
                sshSession);

        Assert.assertEquals(p1, p2);
        Assert.assertEquals(p1.hashCode(), p2.hashCode());
    }
}
