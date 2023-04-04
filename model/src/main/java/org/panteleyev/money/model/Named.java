/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

public interface Named extends Comparable<Named> {
    String name();

    @Override
    default int compareTo(Named other) {
        return this.name().compareTo(other.name());
    }
}
