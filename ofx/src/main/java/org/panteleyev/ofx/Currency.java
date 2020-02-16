package org.panteleyev.ofx;

/*
 Copyright (c) Petr Panteleyev. All rights reserved.
 Licensed under the BSD license. See LICENSE file in the project root for full license information.
 */

import java.math.BigDecimal;

public class Currency {
    private final BigDecimal rate;
    private final String symbol;

    Currency(BigDecimal rate, String symbol) {
        this.rate = rate;
        this.symbol = symbol;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getSymbol() {
        return symbol;
    }
}
