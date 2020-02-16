package org.panteleyev.money.ymoney;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.google.gson.JsonObject;
import java.math.BigDecimal;

public class AccountInfo {
    private final String id;
    private final BigDecimal balance;

    public AccountInfo(JsonObject json) {
        id = json.get("account").getAsString();
        balance = json.get("balance").getAsBigDecimal();
    }

    public String getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
