/*
 Copyright © 2019-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.model;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public record Icon(UUID uuid, String name, byte[] bytes, long created, long modified) implements MoneyRecord {

    public Icon {
        requireNonNull(uuid, "Icon id cannot be null");
        requireNonNull(name, "Icon name cannot be null");
        requireNonNull(bytes, "Icon bytes cannot be null");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Icon that)) {
            return false;
        }

        return Objects.equals(uuid, that.uuid)
            && Objects.equals(name, that.name)
            && Arrays.equals(bytes, that.bytes)
            && created == that.created
            && modified == that.modified;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, Arrays.hashCode(bytes), created, modified);
    }
}
