/*
 Copyright © 2020 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.ofx;

/**
 * This class represents account information.
 *
 * @param type          account type
 * @param bankId        bank id
 * @param accountNumber account number
 */
public record AccountInfo(Type type, String bankId, String accountNumber) {
    public enum Type {
        NONE,
        CHECKING,
        SAVINGS,
        MONEY_MARKET,
        CREDIT_LINE
    }

    AccountInfo() {
        this(Type.NONE, "", "");
    }
}
