/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

public enum PeriodicPaymentType {
    /** Payment must be made manually by the user. */
    MANUAL_PAYMENT,
    /** Auto-payment made by the bank. */
    AUTO_PAYMENT,
    /** Payment is made by the provider via the saved card */
    CARD_PAYMENT
}
