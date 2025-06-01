/*
 Copyright © 2023 Petr Panteleyev <petr-panteleyev@yandex.ru>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.moex.model;

import org.panteleyev.moex.model.MoexEngine;
import org.panteleyev.moex.model.MoexMarket;

public record MoexBoard(
        int id,
        MoexEngine engine,
        MoexMarket market,
        String boardId,
        String boardTitle,
        boolean isTraded,
        boolean hasCandles,
        boolean isPrimary
) {
}
