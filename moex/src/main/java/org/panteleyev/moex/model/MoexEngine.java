/*
 Copyright © 2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

public record MoexEngine(
        int id,
        String name,
        String title
) {
}
