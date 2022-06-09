/*
 Copyright © 2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.xml;

import java.util.UUID;

public record BlobContent(UUID uuid, BlobType type, byte[] bytes) {
    public enum BlobType {
        DOCUMENT
    }
}
