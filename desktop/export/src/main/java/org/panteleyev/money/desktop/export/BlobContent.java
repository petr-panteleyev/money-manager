/*
 Copyright © 2022-2024 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.desktop.export;

import java.util.UUID;

public record BlobContent(UUID uuid, BlobType type, byte[] bytes) {
    public enum BlobType {
        DOCUMENT
    }
}
