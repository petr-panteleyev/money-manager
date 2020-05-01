package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestAuthResponse {
    private static final String REDIRECT_URI = "http://www.panteleyev.org/apps/money-manager/cb";

    @DataProvider(name = "testAuthResponse")
    public Object[][] testAuthResponseDataProvider() {
        return new Object[][]{
            {REDIRECT_URI + "?code=12345678", "12345678", null, null},
            {REDIRECT_URI + "?error=invalid_request", null, "invalid_request", null},
            {REDIRECT_URI + "?error=invalid_request&error_description=Error%20Description",
                null, "invalid_request", "Error Description"},
        };
    }

    @Test(dataProvider = "testAuthResponse")
    public void testCode(String uri, String code, String error, String errorDescription) {
        AuthResponse authResponse = AuthResponse.of(uri);

        Assert.assertEquals(authResponse.code(), code);
        Assert.assertEquals(authResponse.error(), error);
        Assert.assertEquals(authResponse.errorDescription(), errorDescription);
    }
}
