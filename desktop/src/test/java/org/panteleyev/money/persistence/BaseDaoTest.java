/*
 Copyright © 2017-2022 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.money.persistence;

import javafx.embed.swing.JFXPanel;
import org.h2.jdbcx.JdbcDataSource;
import org.panteleyev.money.test.BaseTest;

import java.util.Optional;
import java.util.UUID;

import static org.panteleyev.money.app.GlobalContext.dao;

public class BaseDaoTest extends BaseTest {
    public static final String ICON_DOLLAR = "dollar.png";
    public static final String ICON_EURO = "euro.png";
    public static final String ICON_JAVA = "java.png";

    public void setupAndSkip() throws Exception {
        try {
            var dataSource = new JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            try (var conn = dataSource.getConnection()) {
                new LiquibaseUtil(conn).update();
            }
            dao().initialize(dataSource);
            new JFXPanel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    protected void initializeEmptyMoneyFile() {
        dao().createTables();
        dao().preload(t -> {});
    }

    protected Optional<?> get(Repository<?> repository, UUID uuid) {
        return dao().withNewConnection(connection -> {
            return repository.get(connection, uuid);
        });
    }
}