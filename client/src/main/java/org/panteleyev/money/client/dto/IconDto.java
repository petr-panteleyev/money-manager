/*
 Copyright © 2022 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.client.dto;

import java.util.UUID;

public record IconDto(UUID uuid, long created, long modified) implements MoneyDto {
}
