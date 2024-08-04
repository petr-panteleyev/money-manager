/*
 Copyright © 2017-2023 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.app.account;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.money.app.account.cells.AccountClosingDateCell;
import org.panteleyev.money.model.Account;
import org.panteleyev.money.model.CategoryType;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.money.app.Styles.EXPIRED;

public class AccountClosingDateCellTest {
    private static final int DELTA = 10;

    @BeforeAll
    public static void initFx() {
        new JFXPanel();
    }

    public static List<Arguments> testCellColorDataProvider() {
        return List.of(
                Arguments.of(0, true),
                Arguments.of(9, true),
                Arguments.of(-1, true),
                Arguments.of(10, false),
                Arguments.of(20, false),
                Arguments.of(31, false),
                Arguments.of(90, false),
                Arguments.of(200, false)
        );
    }

    @ParameterizedTest
    @MethodSource("testCellColorDataProvider")
    public void testCellColor(int delta, boolean hasRedColor) {
        var cell = new AccountClosingDateCell(DELTA);

        var today = LocalDate.now();
        var account = new Account.Builder()
                .uuid(UUID.randomUUID())
                .name(UUID.randomUUID().toString())
                .categoryUuid(UUID.randomUUID())
                .type(CategoryType.BANKS_AND_CASH)
                .closingDate(today.plusDays(delta))
                .build();

        cell.updateItem(account, false);
        assertEquals(hasRedColor, cell.getStyleClass().contains(EXPIRED));
    }
}
