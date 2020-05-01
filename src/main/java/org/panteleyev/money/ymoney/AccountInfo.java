package org.panteleyev.money.ymoney;

/*
 * Copyright (c) Petr Panteleyev. All rights reserved.
 * Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import com.google.gson.JsonObject;
import java.math.BigDecimal;

public record AccountInfo(String id, BigDecimal balance) {
    public static AccountInfo of(JsonObject json) {
        return new AccountInfo(json.get("account").getAsString(), json.get("balance").getAsBigDecimal());
    }
}
