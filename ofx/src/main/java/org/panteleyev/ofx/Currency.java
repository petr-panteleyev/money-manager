/*
 Copyright © 2020 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

import java.math.BigDecimal;

public record Currency(BigDecimal rate, String symbol) {
}
