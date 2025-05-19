/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.backend.domain;

import java.util.UUID;

public interface IconSummary {
    UUID getUuid();
    String getName();
    long getCreated();
    long getModified();
}
