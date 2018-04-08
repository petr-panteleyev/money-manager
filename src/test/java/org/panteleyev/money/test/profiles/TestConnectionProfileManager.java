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

package org.panteleyev.money.test.profiles;

import org.panteleyev.money.profiles.ConnectionProfile;
import org.panteleyev.money.profiles.ConnectionProfileManager;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static org.panteleyev.money.test.BaseTestUtils.RANDOM;

public class TestConnectionProfileManager extends ProfileTestBase {
    private static final List<ConnectionProfile> NON_EMPTY_PROFILE_LIST =
            List.of(newProfile(), newProfile(), newTypicalProfile(), newProfile()).stream()
                    .sorted(Comparator.comparing(ConnectionProfile::getName))
                    .collect(Collectors.toList());

    private static final List<ConnectionProfile> EMPTY_PROFILE_LIST = Collections.emptyList();

    private static final ConnectionProfile DEFAULT_PROFILE = NON_EMPTY_PROFILE_LIST.get(0);

    @Test
    public void testSaveLoad() throws Exception {
        boolean autoConnect = RANDOM.nextBoolean();

        ConnectionProfileManager.setAutoConnect(autoConnect);
        ConnectionProfileManager.setProfiles(NON_EMPTY_PROFILE_LIST);
        ConnectionProfileManager.setDefaultProfile(DEFAULT_PROFILE);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ConnectionProfileManager.saveProfiles(out);

            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
                ConnectionProfileManager.setAutoConnect(!autoConnect);
                ConnectionProfileManager.setDefaultProfile(null);
                ConnectionProfileManager.setProfiles(EMPTY_PROFILE_LIST);

                ConnectionProfileManager.loadProfiles(in);

                Assert.assertEquals(ConnectionProfileManager.getAutoConnect(), autoConnect);
                Assert.assertEquals(ConnectionProfileManager.getDefaultProfile(), DEFAULT_PROFILE);
                Assert.assertEquals(
                        ConnectionProfileManager.getAll().stream()
                                .sorted(Comparator.comparing(ConnectionProfile::getName))
                                .collect(Collectors.toList()),
                        NON_EMPTY_PROFILE_LIST);
            }
        }
    }
}
